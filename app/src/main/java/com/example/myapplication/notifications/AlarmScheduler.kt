package com.example.myapplication.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import com.example.myapplication.data.models.Task
import com.example.myapplication.data.models.TaskCategory
import com.example.myapplication.data.models.TaskDifficulty
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Helper class for scheduling alarms that will trigger notifications
 */
class AlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val TAG = "AlarmScheduler"

    /**
     * Schedule an alarm that will trigger a notification for a task
     */
    fun scheduleTaskReminder(task: Task) {
        // Only schedule if there's a due date and reminder is requested
        if (task.dueDate == null || !task.hasReminder) {
            Log.d(TAG, "Not scheduling reminder for task ${task.id}: No due date or reminder not requested")
            return
        }

        // Check if we have permission to schedule exact alarms (for Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.e(TAG, "Cannot schedule exact alarms. Permission not granted.")
                Log.i(TAG, "User needs to go to Settings -> Apps -> Your App -> Special app access -> Alarms & reminders")
                return
            }
        }

        try {
            // Get due date/time as LocalDateTime
            val dueDateTime = task.getDueDateTime()
            if (dueDateTime == null) {
                Log.e(TAG, "Cannot get due date time for task ${task.id}")
                return
            }
            
            // Convert task due date to milliseconds
            val triggerTimeMillis = dueDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val currentTimeMillis = System.currentTimeMillis()
            
            if (triggerTimeMillis <= currentTimeMillis) {
                Log.e(TAG, "Cannot schedule reminder for task ${task.id}: Due time is in the past")
                Log.d(TAG, "Due time: $triggerTimeMillis, Current time: $currentTimeMillis, Difference: ${triggerTimeMillis - currentTimeMillis}ms")
                return
            }
            
            // Log for debugging
            Log.d(TAG, "Creating alarm for task: ${task.id}")
            Log.d(TAG, "Due date: ${task.dueDate}, Due time: ${task.dueTime}")
            Log.d(TAG, "Trigger time: ${triggerTimeMillis} (${java.util.Date(triggerTimeMillis)})")
            Log.d(TAG, "Current time: ${currentTimeMillis} (${java.util.Date(currentTimeMillis)})")
            Log.d(TAG, "Time until alarm: ${(triggerTimeMillis - currentTimeMillis) / 1000} seconds")
            
            // Create intent for the receiver
            val intent = Intent(context, TaskAlarmReceiver::class.java).apply {
                putExtra(TaskAlarmReceiver.EXTRA_TASK_ID, task.id)
                putExtra(TaskAlarmReceiver.EXTRA_TASK_TITLE, task.title)
                putExtra(TaskAlarmReceiver.EXTRA_TASK_DESCRIPTION, task.description ?: "")
                putExtra(TaskAlarmReceiver.EXTRA_TASK_CATEGORY, task.category.name)
                
                // Add a unique action to avoid intent reuse issues
                action = "com.example.myapplication.TASK_ALARM_${task.id}"
            }

            // Create a unique request code based on the task ID
            val requestCode = task.id.hashCode()

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Schedule the alarm
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Log.d(TAG, "Setting exact alarm with allowWhileIdle")
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTimeMillis,
                        pendingIntent
                    )
                } else {
                    Log.d(TAG, "Setting exact alarm")
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        triggerTimeMillis,
                        pendingIntent
                    )
                }
                Log.d(TAG, "Scheduled reminder for task ${task.id} at ${task.dueDate} ${task.dueTime ?: "09:00"}")
                Log.d(TAG, "Trigger time in millis: $triggerTimeMillis, current time: ${System.currentTimeMillis()}")
            } catch (e: Exception) {
                Log.e(TAG, "Error setting alarm: ${e.message}", e)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating intent or pending intent: ${e.message}", e)
        }
    }

    /**
     * Schedule a test notification that will appear in a few seconds
     */
    fun scheduleTestNotification() {
        try {
            // Create a test task
            val testTask = Task(
                title = "Test Notification",
                description = "This is a test notification scheduled to appear in 10 seconds",
                difficulty = TaskDifficulty.MEDIUM,
                category = TaskCategory.OTHER
            )
            
            // Create intent for the receiver
            val intent = Intent(context, TaskAlarmReceiver::class.java).apply {
                putExtra(TaskAlarmReceiver.EXTRA_TASK_ID, testTask.id)
                putExtra(TaskAlarmReceiver.EXTRA_TASK_TITLE, testTask.title)
                putExtra(TaskAlarmReceiver.EXTRA_TASK_DESCRIPTION, testTask.description ?: "")
                putExtra(TaskAlarmReceiver.EXTRA_TASK_CATEGORY, testTask.category.name)
                // Add a specific action for the test notification
                action = "com.example.myapplication.TEST_ALARM"
            }

            // Create a unique request code
            val requestCode = "test_notification".hashCode()

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Schedule the alarm for 10 seconds from now
            val triggerTimeMillis = System.currentTimeMillis() + 10 * 1000

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Log.d(TAG, "Setting test alarm with setExactAndAllowWhileIdle")
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimeMillis,
                    pendingIntent
                )
            } else {
                Log.d(TAG, "Setting test alarm with setExact")
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimeMillis,
                    pendingIntent
                )
            }
            Log.d(TAG, "Scheduled TEST notification to appear in 10 seconds")
            Log.d(TAG, "Trigger time in millis: $triggerTimeMillis, current time: ${System.currentTimeMillis()}")
            
            // For testing, also immediately send a broadcast to the receiver 
            // This helps test if the broadcast receiver logic works without waiting for the alarm
            context.sendBroadcast(intent)
            Log.d(TAG, "Also sent an immediate broadcast for testing")
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling test notification: ${e.message}", e)
        }
    }

    /**
     * Show a notification immediately through NotificationHelper
     */
    fun showImmediateNotification() {
        val notificationHelper = NotificationHelper(context)
        notificationHelper.showTestNotification()
    }

    /**
     * Check if the app has permission to schedule exact alarms
     */
    fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true // Prior to Android 12, no special permission needed
        }
    }

    /**
     * Get the system settings intent for alarm permission
     */
    fun getAlarmPermissionSettingsIntent(): Intent? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
        } else {
            null
        }
    }

    /**
     * Cancel a previously scheduled reminder for a task
     */
    fun cancelTaskReminder(task: Task) {
        val intent = Intent(context, TaskAlarmReceiver::class.java)
        val requestCode = task.id.hashCode()

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

        pendingIntent?.let {
            alarmManager.cancel(it)
            it.cancel()
            Log.d(TAG, "Cancelled reminder for task ${task.id}")
        }
    }

    /**
     * For testing purposes - send a direct broadcast to the TaskAlarmReceiver
     * This bypasses the alarm system entirely
     */
    fun testDirectBroadcast() {
        try {
            val testTask = Task(
                title = "Direct Broadcast Test",
                description = "This bypasses the alarm system entirely",
                difficulty = TaskDifficulty.MEDIUM,
                category = TaskCategory.OTHER
            )
            
            val intent = Intent(context, TaskAlarmReceiver::class.java).apply {
                putExtra(TaskAlarmReceiver.EXTRA_TASK_ID, testTask.id)
                putExtra(TaskAlarmReceiver.EXTRA_TASK_TITLE, testTask.title)
                putExtra(TaskAlarmReceiver.EXTRA_TASK_DESCRIPTION, testTask.description)
                putExtra(TaskAlarmReceiver.EXTRA_TASK_CATEGORY, testTask.category.name)
                action = "com.example.myapplication.DIRECT_TEST"
            }
            
            Log.d(TAG, "Sending direct broadcast to TaskAlarmReceiver")
            context.sendBroadcast(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error sending direct broadcast: ${e.message}", e)
        }
    }
} 