package com.example.myapplication.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.util.Log
import com.example.myapplication.MainActivity
import com.example.myapplication.data.models.Task
import com.example.myapplication.data.models.TaskCategory
import com.example.myapplication.data.models.TaskDifficulty

/**
 * BroadcastReceiver that receives alarms and shows notifications for tasks
 */
class TaskAlarmReceiver : BroadcastReceiver() {

    companion object {
        const val EXTRA_TASK_ID = "extra_task_id"
        const val EXTRA_TASK_TITLE = "extra_task_title"
        const val EXTRA_TASK_DESCRIPTION = "extra_task_description"
        const val EXTRA_TASK_CATEGORY = "extra_task_category"
        private const val TAG = "TaskAlarmReceiver"
        private const val WAKELOCK_TIMEOUT = 10 * 1000L // 10 seconds timeout for wake lock
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Received alarm broadcast: action=${intent.action}")
        Log.d(TAG, "Received at: ${System.currentTimeMillis()} (${java.util.Date()})")
        Log.d(TAG, "Intent extras: ${intent.extras?.keySet()?.joinToString()}")
        
        // Acquire wake lock to ensure notification is shown even if device is sleeping
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "PDAapp:TaskReminderWakeLock"
        )
        wakeLock.acquire(WAKELOCK_TIMEOUT)
        
        try {
            // Extract task information from intent
            val taskId = intent.getStringExtra(EXTRA_TASK_ID)
            if (taskId == null) {
                Log.e(TAG, "Missing task ID in intent extras")
                Log.d(TAG, "Available extras: ${intent.extras?.keySet()?.joinToString() ?: "none"}")
                wakeLock.release()
                return
            }
            
            val taskTitle = intent.getStringExtra(EXTRA_TASK_TITLE) ?: "Task Reminder"
            val taskDescription = intent.getStringExtra(EXTRA_TASK_DESCRIPTION) ?: ""
            val categoryName = intent.getStringExtra(EXTRA_TASK_CATEGORY) ?: TaskCategory.OTHER.name
            
            Log.d(TAG, "Task info: id=$taskId, title=$taskTitle, category=$categoryName")
            
            // Create a mock task object with the data from the intent
            val category = try {
                TaskCategory.valueOf(categoryName)
            } catch (e: IllegalArgumentException) {
                Log.e(TAG, "Invalid category name: $categoryName, using OTHER")
                TaskCategory.OTHER
            }
            
            val task = Task(
                id = taskId,
                title = taskTitle,
                description = taskDescription,
                category = category,
                difficulty = TaskDifficulty.MEDIUM, // Default difficulty
                isCompleted = false,
                hasReminder = true
            )
            
            // Create notification
            val notificationHelper = NotificationHelper(context)
            val hasPermission = notificationHelper.hasNotificationPermission()
            Log.d(TAG, "Notification permission: $hasPermission")
            
            if (hasPermission) {
                try {
                    // Create an intent that will open the main activity without completing the task
                    val mainIntent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        // We can pass the task ID to highlight it in the UI if needed
                        putExtra("taskId", taskId)
                        // Add action to indicate this is coming from a notification
                        action = "com.example.myapplication.OPEN_FROM_NOTIFICATION"
                    }
                    
                    notificationHelper.showTaskReminderNotification(task, mainIntent)
                    Log.d(TAG, "Notification shown for task: $taskId")
                } catch (e: Exception) {
                    Log.e(TAG, "Error showing notification: ${e.message}", e)
                }
            } else {
                Log.e(TAG, "Cannot show notification: No permission")
                // Try to request permission if possible
                Log.d(TAG, "Android version: ${Build.VERSION.SDK_INT}")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Log.d(TAG, "Need to request POST_NOTIFICATIONS permission")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing alarm broadcast: ${e.message}", e)
            e.printStackTrace()
        } finally {
            // Release wake lock
            if (wakeLock.isHeld) {
                wakeLock.release()
                Log.d(TAG, "Wake lock released")
            }
        }
    }
} 