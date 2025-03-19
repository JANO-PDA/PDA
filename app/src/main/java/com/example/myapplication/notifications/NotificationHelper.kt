package com.example.myapplication.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.myapplication.MainActivity
import com.example.myapplication.R
import com.example.myapplication.data.models.Task
import com.example.myapplication.data.models.TaskCategory
import com.example.myapplication.data.models.TaskDifficulty

/**
 * Helper class for managing notifications in the app
 */
class NotificationHelper(private val context: Context) {

    companion object {
        // Channel IDs
        const val CHANNEL_TASK_REMINDERS = "task_reminders"
        
        // Notification IDs
        const val NOTIFICATION_ID_PREFIX = 1000
        
        private const val TAG = "NotificationHelper"
    }

    /**
     * Create notification channels - call this during app initialization
     */
    fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                // Task reminders channel
                val name = "Task Reminders"
                val descriptionText = "Notifications for upcoming tasks"
                val importance = NotificationManager.IMPORTANCE_HIGH
                
                val channel = NotificationChannel(CHANNEL_TASK_REMINDERS, name, importance).apply {
                    description = descriptionText
                    enableVibration(true)
                    enableLights(true)
                }
                
                // Register the channel with the system
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
                Log.d(TAG, "Notification channel created successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error creating notification channel: ${e.message}", e)
            }
        } else {
            Log.d(TAG, "Notification channels not needed for this Android version")
        }
    }

    /**
     * Check if the app has notification permission
     */
    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            Log.d(TAG, "Notification permission check result: $hasPermission")
            hasPermission
        } else {
            Log.d(TAG, "Notification permission check not needed for this Android version")
            true // Prior to Android 13, notification permission is granted at install time
        }
    }

    /**
     * Show a task reminder notification
     */
    fun showTaskReminderNotification(task: Task, customIntent: Intent? = null) {
        Log.d(TAG, "Showing notification for task: ${task.id} - ${task.title}")
        
        // First check permission
        if (!hasNotificationPermission()) {
            Log.e(TAG, "Cannot show notification: Permission not granted")
            return
        }
        
        // Create an intent that opens the app when clicked
        val intent = customIntent ?: Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 
            0, 
            intent, 
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Build the notification
        val builder = NotificationCompat.Builder(context, CHANNEL_TASK_REMINDERS)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // You should replace this with a better icon
            .setContentTitle("Task Reminder: ${task.title}")
            .setContentText(task.description ?: "You have a task due!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        // Add task category-specific styling
        when (task.category) {
            com.example.myapplication.data.models.TaskCategory.WORK -> {
                builder.setColor(0xFF4CAF50.toInt()) // Green color for WORK
            }
            com.example.myapplication.data.models.TaskCategory.STUDY -> {
                builder.setColor(0xFF2196F3.toInt()) // Blue color for STUDY
            }
            com.example.myapplication.data.models.TaskCategory.HEALTH -> {
                builder.setColor(0xFFF44336.toInt()) // Red color for HEALTH
            }
            com.example.myapplication.data.models.TaskCategory.PERSONAL -> {
                builder.setColor(0xFF9C27B0.toInt()) // Purple color for PERSONAL
            }
            com.example.myapplication.data.models.TaskCategory.SHOPPING -> {
                builder.setColor(0xFFFF9800.toInt()) // Orange color for SHOPPING
            }
            else -> {
                builder.setColor(0xFF607D8B.toInt()) // Gray color for OTHER
            }
        }

        // Show the notification
        try {
            with(NotificationManagerCompat.from(context)) {
                val notificationId = NOTIFICATION_ID_PREFIX + task.id.hashCode()
                notify(notificationId, builder.build())
                Log.d(TAG, "Notification shown with ID: $notificationId")
            }
        } catch (e: SecurityException) {
            // Handle the case where notification permission is not granted
            Log.e(TAG, "Security exception when showing notification: ${e.message}", e)
            
            // Alternate approach for testing - try showing a system Toast notification
            try {
                android.widget.Toast.makeText(
                    context, 
                    "Task Reminder: ${task.title}", 
                    android.widget.Toast.LENGTH_LONG
                ).show()
                Log.d(TAG, "Shown Toast notification as fallback")
            } catch (e2: Exception) {
                Log.e(TAG, "Even Toast notification failed: ${e2.message}")
            }
        } catch (e: Exception) {
            // Handle any other exceptions
            Log.e(TAG, "Error showing notification: ${e.message}", e)
        }
    }
    
    /**
     * Show a test notification immediately
     */
    fun showTestNotification() {
        val task = Task(
            title = "Test Notification",
            description = "This is a test notification",
            difficulty = TaskDifficulty.MEDIUM,
            category = TaskCategory.OTHER
        )
        showTaskReminderNotification(task)
    }
} 