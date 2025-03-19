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
    
    // Application context for playing sounds
    private var appContext: Context? = null
    private var completionSoundPlayer: MediaPlayer? = null
    
    // For notifications and alarms
    private lateinit var notificationHelper: NotificationHelper
    private lateinit var alarmScheduler: AlarmScheduler
    
    // For task persistence
    private lateinit var taskStorage: TaskStorage
    
    // Track tasks that are about to become overdue for precise failure messages
    private val tasksChecked = mutableMapOf<String, Boolean>()
    
    // Initialize with application context
    fun initialize(context: Context) {
        try {
            appContext = context
            
            // Create notification helper and channels
            val notificationHelper = NotificationHelper(context)
            notificationHelper.createNotificationChannels()
            
            alarmScheduler = AlarmScheduler(context)
            
            // Initialize task storage and load saved tasks
            taskStorage = TaskStorage(context)
            val savedTasks = taskStorage.loadTasks()
            if (savedTasks.isNotEmpty()) {
                _tasks.value = savedTasks
                // Also update category stats based on loaded tasks
                updateCategoryStats()
                Log.d("TodoViewModel", "Loaded ${savedTasks.size} saved tasks")
            }
            
            // Add a sample NPC message if there are none
            if (npcMessages.value.isEmpty()) {
                val categories = TaskCategory.values()
                for (category in categories) {
                    npcRepository.generateCompletionMessage(category)
                }
            }
            
            // Log for debugging
            Log.d("TodoViewModel", "Initialized notification system")
        } catch (e: Exception) {
            Log.e("TodoViewModel", "Error initializing notification system: ${e.message}", e)
        }
    }
    
    // Helper method to save tasks
    private fun saveTasks() {
        if (::taskStorage.isInitialized) {
            taskStorage.saveTasks(_tasks.value)
        }
    }
    
    // Play task completion sound
    private fun playCompletionSound() {
        appContext?.let { context ->
            try {
                // Release previous player if it exists
                completionSoundPlayer?.release()
                
                // Create a sound programmatically if we can't find a resource
                try {
                    // Try to find the custom sound resource
                    val rawClass = Class.forName("${context.packageName}.R\$raw")
                    val taskCompleteField = rawClass.getDeclaredField("task_complete")
                    val soundResourceId = taskCompleteField.getInt(null)
                    
                    completionSoundPlayer = MediaPlayer.create(context, soundResourceId)
                    completionSoundPlayer?.setOnCompletionListener { it.release() }
                    completionSoundPlayer?.start()
                } catch (e: Exception) {
                    // If we can't find the resource, create a simple beep tone
                    android.media.ToneGenerator(android.media.AudioManager.STREAM_MUSIC, 100)
                        .startTone(android.media.ToneGenerator.TONE_PROP_BEEP, 150)
                }
            } catch (e: Exception) {
                // Ignore errors if sound can't be played
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
        _tasks.update { currentTasks -> currentTasks + newTask }
        
        // Save tasks after adding
        saveTasks()
        
        // Schedule reminder if needed
        if (hasReminder && dueDate != null) {
            try {
                Log.d("TodoViewModel", "Scheduling reminder for task: ${newTask.id}")
                Log.d("TodoViewModel", "Task due date: $dueDate, due time: ${dueTime ?: "not set"}")
                
                // Calculate milliseconds until the due time for logging
                val dueDateTime = newTask.getDueDateTime()
                val dueDateTimeMillis = dueDateTime?.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli() ?: 0
                val currentTimeMillis = System.currentTimeMillis()
                val millisUntilDue = dueDateTimeMillis - currentTimeMillis
                
                Log.d("TodoViewModel", "Due time in millis: $dueDateTimeMillis")
                Log.d("TodoViewModel", "Current time millis: $currentTimeMillis")
                Log.d("TodoViewModel", "Milliseconds until due: $millisUntilDue (${millisUntilDue / (1000 * 60)} minutes)")
                
                // Only schedule if the due time is in the future
                if (millisUntilDue > 0) {
                    alarmScheduler.scheduleTaskReminder(newTask)
                    Log.d("TodoViewModel", "Reminder scheduled successfully")
                } else {
                    Log.e("TodoViewModel", "Cannot schedule reminder: Due time is in the past")
                }
            } catch (e: Exception) {
                Log.e("TodoViewModel", "Error scheduling reminder: ${e.message}", e)
                // Continue with adding the task even if reminder scheduling fails
            }
        }
        
        // Update category stats
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
        
        _tasks.update { currentTasks -> 
            currentTasks + newSubtask
        }
        
        // Update the parent task to reference this subtask
        val updatedParentTask = parentTask.copy(
            subtasks = parentTask.subtasks + newSubtask.id
        )
        
        _tasks.update { currentTasks ->
            currentTasks.map { if (it.id == parentTaskId) updatedParentTask else it }
        }
        
        // Save tasks after adding subtask
        saveTasks()
        
        // Update category stats
        updateCategoryStats()
    }
    
    fun showAddSubtaskDialog(parentTask: Task) {
        _addSubtaskFor.value = parentTask
        // Reset form fields
        subtaskTitleValue = ""
        subtaskDescriptionValue = ""
        subtaskDifficultyValue = TaskDifficulty.EASY
    }
    
    fun dismissAddSubtaskDialog() {
        _addSubtaskFor.value = null
    }

    fun completeTask(task: Task) {
        // Set task as completed
        val updatedTask = task.copy(
            isCompleted = true,
            completedAt = System.currentTimeMillis()
        )
        
        // Update task in list
        _tasks.update { currentTasks ->
            currentTasks.map {
                if (it.id == task.id) updatedTask else it
            }
        }
        
        if (!task.isSubtask()) {
            // Generate NPC message for task completion
            npcRepository.generateCompletionMessage(task.category)
        }
        
        // Only award XP for parent tasks or standalone tasks (not subtasks)
        if (!task.isSubtask()) {
            awardXpForTask(task)
        }
        
        // Track completion in category stats
        updateCategoryStats()

        // Show confetti animation
        _showConfetti.value = true
        viewModelScope.launch {
            delay(3000)  // Show confetti for 3 seconds
            _showConfetti.value = false
        }
        
        // Save tasks
        saveTasks()
    }

    // Separate function to complete only a subtask without completing parent
    fun completeSubtask(subtask: Task) {
        if (subtask.isCompleted || subtask.parentTaskId == null) return
        
        val completedSubtask = subtask.copy(
            isCompleted = true,
            completedAt = System.currentTimeMillis()
        )
        
        _tasks.update { currentTasks ->
            currentTasks.map { 
                if (it.id == subtask.id) completedSubtask else it 
            }
        }
        
        // Save tasks after completion
        saveTasks()
        
        // Update category stats
        updateCategoryStats()
        
        // Apply XP bonus for completion
        val xpGained = subtask.getXpReward()
        
        // Update user profile 
        _userProfile.update { currentProfile ->
            // Increase category-specific XP
            val currentCategoryXp = currentProfile.categoryXp[subtask.category] ?: 0
            val updatedCategoryXp = currentProfile.categoryXp.toMutableMap().apply {
                this[subtask.category] = currentCategoryXp + xpGained
            }
            
            // Increment tasks completed counter for this category
            val currentTasksCompleted = currentProfile.categoryTasksCompleted[subtask.category] ?: 0
            val updatedTasksCompleted = currentProfile.categoryTasksCompleted.toMutableMap().apply {
                this[subtask.category] = currentTasksCompleted + 1
            }
            
            // Calculate new level based on total XP
            val totalXp = updatedCategoryXp.values.sum()
            val newLevel = calculateLevel(totalXp)
            
            // Calculate new category levels
            val categoryLevels = updatedCategoryXp.mapValues { (_, xp) -> calculateLevel(xp) }
            
            currentProfile.copy(
                totalXp = totalXp,
                level = newLevel,
                categoryXp = updatedCategoryXp,
                categoryTasksCompleted = updatedTasksCompleted,
                categoryLevels = categoryLevels
            )
        }
        
        // Play completion sound
        playCompletionSound()
    }

    fun deleteTask(task: Task) {
        Log.d("TodoViewModel", "Deleting task: ${task.id} - ${task.title}")
        
        // If the task was completed, remove XP
        if (task.isCompleted && !task.isSubtask()) {
            removeXpForTask(task)
        }
        
        // Generate NPC message for task deletion ONLY if the task is not overdue
        // This prevents duplicate messages for overdue tasks that are then deleted
        if (!task.isSubtask() && !task.isOverdue()) {
            // Generate failure message only if task wasn't already overdue
            npcRepository.generateFailureMessage(task.category)
            Log.d("TodoViewModel", "Generated failure message for deleted task: ${task.id} - ${task.title}")
        } else {
            Log.d("TodoViewModel", "Skipped generating failure message for deleted task: ${task.id} - Task was already overdue")
        }
        
        // Delete task and its subtasks
        _tasks.update { currentTasks ->
            currentTasks.filter { 
                it.id != task.id && it.parentTaskId != task.id 
            }
        }
        
        // Update category stats
        updateCategoryStats()
        
        // Save tasks
        saveTasks()
    }
    
    fun deleteAllCompletedTasks() {
        val completedTasks = _tasks.value.filter { it.isCompleted }
        
        _tasks.update { currentTasks -> 
            currentTasks.filter { !it.isCompleted }
        }
        
        // Save tasks after deletion
        saveTasks()
        
        // Cancel any reminders for completed tasks
        completedTasks.forEach { task ->
            if (task.hasReminder) {
                alarmScheduler.cancelTaskReminder(task)
            }
        }
        
        // Update category stats
        updateCategoryStats()
    }

    fun updateTheme(theme: AppTheme) {
        _userProfile.update { it.copy(selectedTheme = theme) }
    }
    
    // Function to update category statistics
    fun updateCategoryStats() {
        val tasksByCategory = _tasks.value.groupBy { it.category }
        
        _categoryStats.update { currentStats ->
            val newStats = currentStats.toMutableMap()
            
            TaskCategory.values().forEach { category ->
                val categoryTasks = tasksByCategory[category] ?: emptyList()
                val completedTasks = categoryTasks.count { it.isCompleted }
                val totalTasks = categoryTasks.size
                val completionRate = if (totalTasks > 0) completedTasks.toFloat() / totalTasks else 0f
                
                newStats[category] = CategoryStats(
                    totalTasks = totalTasks,
                    completedTasks = completedTasks,
                    completionRate = completionRate
                )
            }
            
            newStats
        }
    }

    // Function to set confetti position
    fun setConfettiPosition(x: Float, y: Float) {
        _confettiPosition.value = Pair(x, y)
    }
    
    // Function to trigger confetti animation
    fun showConfetti() {
        viewModelScope.launch {
            _showConfetti.value = true
            delay(2000) // Duration of confetti animation
            _showConfetti.value = false
        }
    }
    
    // Test functions for notifications
    fun scheduleTestNotification() {
        try {
            Log.d("TodoViewModel", "Scheduling test notification")
            alarmScheduler.scheduleTestNotification()
        } catch (e: Exception) {
            Log.e("TodoViewModel", "Error scheduling test notification: ${e.message}", e)
        }
    }
    
    fun showImmediateNotification() {
        try {
            Log.d("TodoViewModel", "Showing immediate test notification")
            alarmScheduler.showImmediateNotification()
        } catch (e: Exception) {
            Log.e("TodoViewModel", "Error showing immediate notification: ${e.message}", e)
        }
    }
    
    fun testDirectBroadcast() {
        try {
            Log.d("TodoViewModel", "Testing direct broadcast")
            alarmScheduler.testDirectBroadcast()
        } catch (e: Exception) {
            Log.e("TodoViewModel", "Error sending direct broadcast: ${e.message}", e)
        }
    }
    
    fun checkNotificationPermissions(): Boolean {
        val notificationHelper = NotificationHelper(appContext ?: return false)
        return notificationHelper.hasNotificationPermission()
    }
    
    fun checkAlarmPermissions(): Boolean {
        return alarmScheduler.canScheduleExactAlarms()
    }
    
    fun openAlarmPermissionSettings() {
        try {
            val settingsIntent = alarmScheduler.getAlarmPermissionSettingsIntent()
            if (settingsIntent != null) {
                // Make sure we have an activity context to start the intent
                appContext?.let { context ->
                    if (context !is android.app.Activity) {
                        // If we don't have an activity context, we need to add the FLAG_ACTIVITY_NEW_TASK
                        settingsIntent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(settingsIntent)
                    Log.d("TodoViewModel", "Opened alarm permission settings")
                }
            } else {
                Log.e("TodoViewModel", "Couldn't get alarm settings intent")
            }
        } catch (e: Exception) {
            Log.e("TodoViewModel", "Error opening alarm settings: ${e.message}", e)
        }
    }
    
    // For highlighting tasks opened from notifications
    private val _highlightedTaskId = MutableStateFlow<String?>(null)
    val highlightedTaskId: StateFlow<String?> = _highlightedTaskId.asStateFlow()
    
    fun highlightTask(taskId: String) {
        Log.d("TodoViewModel", "Highlighting task: $taskId")
        _highlightedTaskId.value = taskId
        
        // Find the task to make sure it exists
        val task = _tasks.value.find { it.id == taskId }
        if (task != null) {
            Log.d("TodoViewModel", "Found task to highlight: ${task.title}")
        } else {
            Log.e("TodoViewModel", "Task not found for highlighting: $taskId")
        }
        
        // Clear highlight after a short delay
        viewModelScope.launch {
            delay(5000) // Clear highlight after 5 seconds
            _highlightedTaskId.value = null
        }
    }

    // Function to open contacts screen
    fun openContactsScreen() {
        _isContactsScreenOpen.value = true
    }
    
    // Function to close contacts screen
    fun closeContactsScreen() {
        _isContactsScreenOpen.value = false
    }
    
    // Get unread message count
    fun getUnreadMessageCount(): Int {
        return npcRepository.getUnreadMessageCount()
    }
    
    // Mark message as read
    fun markMessageAsRead(messageId: String) {
        npcRepository.markMessageAsRead(messageId)
    }
    
    // Mark all messages as read
    fun markAllMessagesAsRead() {
        npcRepository.markAllMessagesAsRead()
    }

    // Award XP for completing a task
    private fun awardXpForTask(task: Task) {
        val xpGained = getXpForTask(task)
        
        _userProfile.update { currentProfile ->
            // Update total XP
            val newTotalXp = currentProfile.totalXp + xpGained
            
            // Update category XP
            val newCategoryXpMap = currentProfile.categoryXp.toMutableMap()
            val currentCategoryXp = currentProfile.categoryXp[task.category] ?: 0
            newCategoryXpMap[task.category] = currentCategoryXp + xpGained
            
            // Update category tasks completed
            val newCategoryTasksCompletedMap = currentProfile.categoryTasksCompleted.toMutableMap()
            val currentTasksCompleted = currentProfile.categoryTasksCompleted[task.category] ?: 0
            newCategoryTasksCompletedMap[task.category] = currentTasksCompleted + 1
            
            currentProfile.copy(
                totalXp = newTotalXp,
                level = calculateLevel(newTotalXp),
                categoryXp = newCategoryXpMap,
                categoryTasksCompleted = newCategoryTasksCompletedMap
            )
        }
    }
    
    // Remove XP for a completed task that was deleted
    private fun removeXpForTask(task: Task) {
        val xpToRemove = getXpForTask(task)
        
        _userProfile.update { currentProfile ->
            // Update total XP
            val newTotalXp = (currentProfile.totalXp - xpToRemove).coerceAtLeast(0)
            
            // Update category XP
            val newCategoryXpMap = currentProfile.categoryXp.toMutableMap()
            val currentCategoryXp = currentProfile.categoryXp[task.category] ?: 0
            newCategoryXpMap[task.category] = (currentCategoryXp - xpToRemove).coerceAtLeast(0)
            
            // Update category tasks completed
            val newCategoryTasksCompletedMap = currentProfile.categoryTasksCompleted.toMutableMap()
            val currentTasksCompleted = currentProfile.categoryTasksCompleted[task.category] ?: 0
            newCategoryTasksCompletedMap[task.category] = (currentTasksCompleted - 1).coerceAtLeast(0)
            
            currentProfile.copy(
                totalXp = newTotalXp,
                level = calculateLevel(newTotalXp),
                categoryXp = newCategoryXpMap,
                categoryTasksCompleted = newCategoryTasksCompletedMap
            )
        }
    }
    
    // Calculate XP for a task based on its difficulty
    private fun getXpForTask(task: Task): Int {
        return when(task.difficulty) {
            TaskDifficulty.EASY -> 10
            TaskDifficulty.MEDIUM -> 20
            TaskDifficulty.HARD -> 30
            TaskDifficulty.NIGHTMARE -> 50
        }
    }

    // Check for overdue tasks and generate failure messages at the exact time
    private fun checkOverdueTasks() {
        val now = System.currentTimeMillis()
        val allTasks = _tasks.value
        
        // Find tasks that are exactly becoming overdue in this check
        allTasks.forEach { task ->
            if (!task.isCompleted && !task.isSubtask() && task.dueDate != null) {
                val taskId = task.id
                
                // Get the due time in milliseconds
                val dueDateTime = task.getDueDateTime()
                val dueTimeMillis = dueDateTime?.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli() ?: 0
                
                // If the task is becoming overdue right now (within 1 second precision)
                val isOverdue = task.isOverdue()
                val wasCheckedBefore = tasksChecked[taskId] ?: false
                
                // If we just detected task becoming overdue (was not overdue before, is overdue now)
                if (isOverdue && !wasCheckedBefore) {
                    // Generate NPC message for task failure at the exact time
                    npcRepository.generateFailureMessage(task.category)
                    
                    // Log for debugging
                    Log.d("TodoViewModel", "Task became overdue exactly now: ${task.id} - ${task.title}")
                    Log.d("TodoViewModel", "Due time: ${dueDateTime}, Current time: ${java.time.LocalDateTime.now()}")
                    Log.d("TodoViewModel", "Generated failure message for overdue task")
                }
                
                // Update the checked status for this task
                tasksChecked[taskId] = isOverdue
            }
        }
        
        // Clean up tasks that no longer exist or are completed
        val validTaskIds = allTasks.filter { !it.isCompleted }.map { it.id }.toSet()
        tasksChecked.keys.toList().forEach { taskId ->
            if (taskId !in validTaskIds) {
                tasksChecked.remove(taskId)
            }
        }
    }
    
    // Function to check for overdue tasks - exposed for external calls
    fun checkForOverdueTasks() {
        checkOverdueTasks()
    }

    // Function to generate a debug failure message
    fun generateDebugFailureMessage() {
        // Use a default category for testing
        val testCategory = TaskCategory.WORK
        npcRepository.generateFailureMessage(testCategory)
        
        // Log for debugging
        Log.d("TodoViewModel", "Generated debug failure message for category: ${testCategory.name}")
    }

    init {
        // Check for overdue tasks on initialization
        viewModelScope.launch {
            checkOverdueTasks()
        }
    }
}

data class CategoryStats(
    val totalTasks: Int = 0,
    val completedTasks: Int = 0,
    val completionRate: Float = 0f
) 