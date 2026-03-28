package com.example.myapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.models.Task
import com.example.myapplication.data.models.TaskCategory
import com.example.myapplication.data.models.TaskDifficulty
import com.example.myapplication.data.models.UserProfile
import com.example.myapplication.data.models.AppTheme
import com.example.myapplication.data.models.calculateLevel
import com.example.myapplication.data.models.NpcMessage
import com.example.myapplication.data.repository.NpcRepository
import android.content.Context
import android.media.MediaPlayer
import android.media.ToneGenerator
import android.media.AudioManager
import android.util.Log
import com.example.myapplication.notifications.AlarmScheduler
import com.example.myapplication.notifications.NotificationHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.Instant
import java.time.Duration
import com.example.myapplication.data.TaskStorage
import com.example.myapplication.data.UserProfileStorage
import android.os.Build
import java.util.UUID

class TodoViewModel : ViewModel() {
    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    // NPC Repository
    private val npcRepository = NpcRepository()
    val npcMessages = npcRepository.messages
    val npcs = npcRepository.npcs

    // Track if contacts screen is open
    private val _isContactsScreenOpen = MutableStateFlow(false)
    val isContactsScreenOpen: StateFlow<Boolean> = _isContactsScreenOpen.asStateFlow()

    // Category stats
    private val _categoryStats = MutableStateFlow<Map<TaskCategory, CategoryStats>>(
        TaskCategory.values().associateWith { CategoryStats() }
    )
    val categoryStats: StateFlow<Map<TaskCategory, CategoryStats>> = _categoryStats.asStateFlow()

    // Subtask dialog state
    private val _addSubtaskFor = MutableStateFlow<Task?>(null)
    val addSubtaskFor: StateFlow<Task?> = _addSubtaskFor.asStateFlow()

    // State for showing confetti animation
    private val _showConfetti = MutableStateFlow(false)
    val showConfetti: StateFlow<Boolean> = _showConfetti.asStateFlow()

    // Track confetti position (to center on completed task)
    private val _confettiPosition = MutableStateFlow(Pair(0f, 0f))
    val confettiPosition: StateFlow<Pair<Float, Float>> = _confettiPosition.asStateFlow()

    // Track the most recently created task ID
    private val _lastCreatedTaskId = MutableStateFlow("")
    val lastCreatedTaskId: StateFlow<String> = _lastCreatedTaskId.asStateFlow()

    // Application context for playing sounds
    private var appContext: Context? = null
    private var completionSoundPlayer: MediaPlayer? = null

    // For notifications and alarms
    private lateinit var notificationHelper: NotificationHelper
    private lateinit var alarmScheduler: AlarmScheduler

    // For persistence
    private lateinit var taskStorage: TaskStorage
    private lateinit var userProfileStorage: UserProfileStorage

    // Track tasks that are about to become overdue for precise failure messages
    private val tasksChecked = mutableMapOf<String, Boolean>()

    fun initialize(context: Context) {
        try {
            appContext = context

            notificationHelper = NotificationHelper(context)
            notificationHelper.createNotificationChannels()

            alarmScheduler = AlarmScheduler(context)

            // Load tasks
            taskStorage = TaskStorage(context)
            val savedTasks = taskStorage.loadTasks()
            if (savedTasks.isNotEmpty()) {
                _tasks.value = savedTasks
                updateCategoryStats()
                Log.d("TodoViewModel", "Loaded ${savedTasks.size} saved tasks")
            }

            // Load user profile (Fix 1: profile now persists across restarts)
            userProfileStorage = UserProfileStorage(context)
            _userProfile.value = userProfileStorage.loadProfile()
            Log.d("TodoViewModel", "Loaded user profile (level=${_userProfile.value.level}, xp=${_userProfile.value.totalXp})")

            if (npcMessages.value.isEmpty()) {
                for (category in TaskCategory.values()) {
                    npcRepository.generateCompletionMessage(category)
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val canScheduleAlarms = alarmScheduler.canScheduleExactAlarms()
                Log.d("TodoViewModel", "Can schedule exact alarms: $canScheduleAlarms")
                if (!canScheduleAlarms) {
                    Log.e("TodoViewModel", "Alarm permission not granted! Notifications for due tasks won't work")
                }
            }

            Log.d("TodoViewModel", "Notification permission: ${notificationHelper.hasNotificationPermission()}")
            Log.d("TodoViewModel", "Initialized notification system")
        } catch (e: Exception) {
            Log.e("TodoViewModel", "Error initializing: ${e.message}", e)
        }
    }

    private fun saveTasks() {
        if (::taskStorage.isInitialized) {
            taskStorage.saveTasks(_tasks.value)
        }
    }

    private fun saveProfile() {
        if (::userProfileStorage.isInitialized) {
            userProfileStorage.saveProfile(_userProfile.value)
        }
    }

    private fun playCompletionSound() {
        appContext?.let { context ->
            try {
                completionSoundPlayer?.release()
                try {
                    val rawClass = Class.forName("${context.packageName}.R\$raw")
                    val taskCompleteField = rawClass.getDeclaredField("task_complete")
                    val soundResourceId = taskCompleteField.getInt(null)
                    completionSoundPlayer = MediaPlayer.create(context, soundResourceId)
                    completionSoundPlayer?.setOnCompletionListener { it.release() }
                    completionSoundPlayer?.start()
                } catch (e: Exception) {
                    android.media.ToneGenerator(android.media.AudioManager.STREAM_MUSIC, 100)
                        .startTone(android.media.ToneGenerator.TONE_PROP_BEEP, 150)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun addTask(
        title: String,
        description: String,
        difficulty: TaskDifficulty,
        category: TaskCategory,
        dueDate: LocalDate? = null,
        dueTime: LocalTime? = null,
        hasReminder: Boolean = false
    ) {
        val newTask = Task(
            title = title,
            description = description,
            difficulty = difficulty,
            category = category,
            dueDate = dueDate,
            dueTime = dueTime,
            hasReminder = hasReminder
        )
        _tasks.update { it + newTask }
        saveTasks()

        if (hasReminder && dueDate != null) {
            try {
                val dueDateTime = newTask.getDueDateTime()
                val dueDateTimeMillis = dueDateTime?.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli() ?: 0
                val millisUntilDue = dueDateTimeMillis - System.currentTimeMillis()
                if (millisUntilDue > 0) {
                    alarmScheduler.scheduleTaskReminder(newTask)
                    Log.d("TodoViewModel", "Reminder scheduled for task: ${newTask.id}")
                } else {
                    Log.e("TodoViewModel", "Cannot schedule reminder: due time is in the past")
                }
            } catch (e: Exception) {
                Log.e("TodoViewModel", "Error scheduling reminder: ${e.message}", e)
            }
        }

        updateCategoryStats()
        _lastCreatedTaskId.value = newTask.id
    }

    // Fix 9: Update an existing task (for edit support)
    fun updateTask(updated: Task) {
        _tasks.update { currentTasks ->
            currentTasks.map { if (it.id == updated.id) updated else it }
        }
        saveTasks()
        updateCategoryStats()
    }

    fun addSubtask(
        parentTaskId: String,
        title: String,
        description: String,
        difficulty: TaskDifficulty
    ) {
        val parentTask = _tasks.value.find { it.id == parentTaskId } ?: return

        val newSubtask = Task(
            title = title,
            description = description,
            difficulty = difficulty,
            category = parentTask.category,
            parentTaskId = parentTaskId
        )

        _tasks.update { it + newSubtask }

        val updatedParent = parentTask.copy(subtasks = parentTask.subtasks + newSubtask.id)
        _tasks.update { currentTasks ->
            currentTasks.map { if (it.id == parentTaskId) updatedParent else it }
        }

        saveTasks()
        updateCategoryStats()
    }

    fun showAddSubtaskDialog(parentTask: Task) {
        _addSubtaskFor.value = parentTask
        subtaskTitleValue = ""
        subtaskDescriptionValue = ""
        subtaskDifficultyValue = TaskDifficulty.EASY
    }

    fun setAddSubtaskFor(task: Task) {
        _addSubtaskFor.value = task
    }

    fun dismissAddSubtaskDialog() {
        _addSubtaskFor.value = null
    }

    fun completeTask(task: Task) {
        val updatedTask = task.copy(isCompleted = true, completedAt = System.currentTimeMillis())

        _tasks.update { currentTasks ->
            currentTasks.map { if (it.id == task.id) updatedTask else it }
        }

        if (!task.isSubtask()) {
            npcRepository.generateCompletionMessage(task.category)
            awardXpForTask(task)  // Fix 2: uses task.getXpReward() and saves profile
            updateStreak()         // Fix 8: streak tracking
        }

        updateCategoryStats()
        showConfetti()             // Fix 3: confetti now enabled
        saveTasks()
    }

    fun completeSubtask(subtask: Task) {
        if (subtask.isCompleted || subtask.parentTaskId == null) return

        val completedSubtask = subtask.copy(isCompleted = true, completedAt = System.currentTimeMillis())
        _tasks.update { currentTasks ->
            currentTasks.map { if (it.id == subtask.id) completedSubtask else it }
        }

        saveTasks()
        updateCategoryStats()

        val xpGained = subtask.getXpReward()  // Fix 2: consistent XP values

        _userProfile.update { currentProfile ->
            val currentCategoryXp = currentProfile.categoryXp[subtask.category] ?: 0
            val updatedCategoryXp = currentProfile.categoryXp.toMutableMap().apply {
                this[subtask.category] = currentCategoryXp + xpGained
            }
            val currentTasksCompleted = currentProfile.categoryTasksCompleted[subtask.category] ?: 0
            val updatedTasksCompleted = currentProfile.categoryTasksCompleted.toMutableMap().apply {
                this[subtask.category] = currentTasksCompleted + 1
            }
            val totalXp = updatedCategoryXp.values.sum()
            currentProfile.copy(
                totalXp = totalXp,
                level = calculateLevel(totalXp),
                categoryXp = updatedCategoryXp,
                categoryTasksCompleted = updatedTasksCompleted,
                categoryLevels = updatedCategoryXp.mapValues { (_, xp) -> calculateLevel(xp) }
            )
        }

        saveProfile()
        playCompletionSound()
    }

    fun deleteTask(task: Task) {
        Log.d("TodoViewModel", "Deleting task: ${task.id} - ${task.title}")

        if (task.isCompleted && !task.isSubtask()) {
            removeXpForTask(task)
        }

        if (!task.isSubtask() && !task.isOverdue()) {
            npcRepository.generateFailureMessage(task.category)
        }

        _tasks.update { currentTasks ->
            currentTasks.filter { it.id != task.id && it.parentTaskId != task.id }
        }

        updateCategoryStats()
        saveTasks()
    }

    fun deleteAllCompletedTasks() {
        _tasks.value = _tasks.value.filter { !it.isCompleted }
        saveTasks()
        updateCategoryStats()
    }

    fun updateTheme(theme: AppTheme) {
        _userProfile.update { it.copy(selectedTheme = theme) }
        saveProfile()
    }

    fun updateCategoryStats() {
        val tasksByCategory = _tasks.value.groupBy { it.category }

        _categoryStats.update { currentStats ->
            val newStats = currentStats.toMutableMap()
            TaskCategory.values().forEach { category ->
                val categoryTasks = tasksByCategory[category] ?: emptyList()
                val completedTasks = categoryTasks.count { it.isCompleted }
                val totalTasks = categoryTasks.size
                newStats[category] = CategoryStats(
                    totalTasks = totalTasks,
                    completedTasks = completedTasks,
                    completionRate = if (totalTasks > 0) completedTasks.toFloat() / totalTasks else 0f
                )
            }
            newStats
        }
    }

    fun setConfettiPosition(x: Float, y: Float) {
        _confettiPosition.value = Pair(x, y)
    }

    fun showConfetti() {
        viewModelScope.launch {
            _showConfetti.value = true
            delay(2000)
            _showConfetti.value = false
        }
    }

    fun scheduleTestNotification() {
        try {
            alarmScheduler.scheduleTestNotification()
        } catch (e: Exception) {
            Log.e("TodoViewModel", "Error scheduling test notification: ${e.message}", e)
        }
    }

    fun showImmediateNotification() {
        try {
            alarmScheduler.showImmediateNotification()
        } catch (e: Exception) {
            Log.e("TodoViewModel", "Error showing immediate notification: ${e.message}", e)
        }
    }

    fun testDirectBroadcast() {
        try {
            alarmScheduler.testDirectBroadcast()
        } catch (e: Exception) {
            Log.e("TodoViewModel", "Error sending direct broadcast: ${e.message}", e)
        }
    }

    fun checkNotificationPermissions(): Boolean {
        val helper = NotificationHelper(appContext ?: return false)
        return helper.hasNotificationPermission()
    }

    fun checkAlarmPermissions(): Boolean {
        return alarmScheduler.canScheduleExactAlarms()
    }

    fun openAlarmPermissionSettings() {
        try {
            val settingsIntent = alarmScheduler.getAlarmPermissionSettingsIntent() ?: return
            appContext?.let { context ->
                if (context !is android.app.Activity) {
                    settingsIntent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(settingsIntent)
            }
        } catch (e: Exception) {
            Log.e("TodoViewModel", "Error opening alarm settings: ${e.message}", e)
        }
    }

    private val _highlightedTaskId = MutableStateFlow<String?>(null)
    val highlightedTaskId: StateFlow<String?> = _highlightedTaskId.asStateFlow()

    fun highlightTask(taskId: String) {
        _highlightedTaskId.value = taskId
        val task = _tasks.value.find { it.id == taskId }
        Log.d("TodoViewModel", if (task != null) "Highlighting: ${task.title}" else "Task not found: $taskId")
        viewModelScope.launch {
            delay(5000)
            _highlightedTaskId.value = null
        }
    }

    fun openContactsScreen() { _isContactsScreenOpen.value = true }
    fun closeContactsScreen() { _isContactsScreenOpen.value = false }

    fun getUnreadMessageCount(): Int = npcMessages.value.count { !it.isRead }
    fun markMessageAsRead(messageId: String) = npcRepository.markMessageAsRead(messageId)
    fun markAllMessagesAsRead() = npcRepository.markAllMessagesAsRead()

    // Fix 2: XP unified — uses task.getXpReward() (10/25/50/100) everywhere
    private fun awardXpForTask(task: Task) {
        val xpGained = task.getXpReward()

        _userProfile.update { currentProfile ->
            val newTotalXp = currentProfile.totalXp + xpGained
            val newCategoryXp = currentProfile.categoryXp.toMutableMap().apply {
                this[task.category] = (this[task.category] ?: 0) + xpGained
            }
            val newCategoryTasksDone = currentProfile.categoryTasksCompleted.toMutableMap().apply {
                this[task.category] = (this[task.category] ?: 0) + 1
            }
            currentProfile.copy(
                totalXp = newTotalXp,
                level = calculateLevel(newTotalXp),
                categoryXp = newCategoryXp,
                categoryTasksCompleted = newCategoryTasksDone
            )
        }

        saveProfile()
        playCompletionSound()
    }

    private fun removeXpForTask(task: Task) {
        val xpToRemove = task.getXpReward()

        _userProfile.update { currentProfile ->
            val newTotalXp = (currentProfile.totalXp - xpToRemove).coerceAtLeast(0)
            val newCategoryXp = currentProfile.categoryXp.toMutableMap().apply {
                this[task.category] = ((this[task.category] ?: 0) - xpToRemove).coerceAtLeast(0)
            }
            val newCategoryTasksDone = currentProfile.categoryTasksCompleted.toMutableMap().apply {
                this[task.category] = ((this[task.category] ?: 0) - 1).coerceAtLeast(0)
            }
            currentProfile.copy(
                totalXp = newTotalXp,
                level = calculateLevel(newTotalXp),
                categoryXp = newCategoryXp,
                categoryTasksCompleted = newCategoryTasksDone
            )
        }

        saveProfile()
    }

    // Fix 8: Streak tracking logic
    private fun updateStreak() {
        val today = LocalDate.now()
        val todayStart = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val yesterdayStart = today.minusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

        _userProfile.update { profile ->
            val last = profile.lastCompletedTaskDate
            val newStreak = when {
                last == null -> 1
                last >= todayStart -> profile.taskStreak          // already completed today
                last >= yesterdayStart -> profile.taskStreak + 1  // consecutive day
                else -> 1                                          // streak broken
            }
            profile.copy(taskStreak = newStreak, lastCompletedTaskDate = todayStart)
        }
    }

    private fun checkOverdueTasks() {
        val allTasks = _tasks.value

        allTasks.forEach { task ->
            if (!task.isCompleted && !task.isSubtask() && task.dueDate != null) {
                val taskId = task.id
                val isOverdue = task.isOverdue()
                val wasCheckedBefore = tasksChecked[taskId] ?: false

                if (isOverdue && !wasCheckedBefore) {
                    npcRepository.generateFailureMessage(task.category)
                    Log.d("TodoViewModel", "Task became overdue: ${task.id} - ${task.title}")
                }

                tasksChecked[taskId] = isOverdue
            }
        }

        val validTaskIds = allTasks.filter { !it.isCompleted }.map { it.id }.toSet()
        tasksChecked.keys.toList().forEach { if (it !in validTaskIds) tasksChecked.remove(it) }
    }

    fun checkForOverdueTasks() = checkOverdueTasks()

    fun updateUserProfile(updatedProfile: UserProfile) {
        _userProfile.value = updatedProfile
        saveProfile()
    }

    fun setAppTheme(theme: AppTheme) {
        _userProfile.update { it.copy(selectedTheme = theme) }
        saveProfile()
    }

    fun setDarkMode(darkMode: Boolean?) {
        _userProfile.update { it.copy(darkMode = darkMode) }
        saveProfile()
    }

    init {
        viewModelScope.launch { checkOverdueTasks() }
    }
}

data class CategoryStats(
    val totalTasks: Int = 0,
    val completedTasks: Int = 0,
    val completionRate: Float = 0f
)
