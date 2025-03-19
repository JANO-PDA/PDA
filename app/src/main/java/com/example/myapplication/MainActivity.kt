package com.example.myapplication

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.myapplication.notifications.RequestNotificationPermission
import com.example.myapplication.ui.screens.MainScreen
import com.example.myapplication.ui.theme.TodoAppTheme
import com.example.myapplication.ui.viewmodel.TodoViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: TodoViewModel by viewModels()
    private val TAG = "MainActivity"
    
    // Handler for periodic checks
    private val handler = Handler(Looper.getMainLooper())
    private val checkOverdueRunnable = object : Runnable {
        override fun run() {
            viewModel.checkForOverdueTasks()
            Log.d("MainActivity", "Periodic overdue task check executed")
            // Run the check every minute instead of every second
            handler.postDelayed(this, 60000) // 60 seconds instead of 1 second
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize ViewModel with context for sound playback
        viewModel.initialize(this)
        
        // Handle notification intent if present
        handleIntent(intent)
        
        // Don't test notification system on every start - this can cause confusion with unexpected notifications
        // testNotificationSystem()
        
        setContent {
            val userProfile by viewModel.userProfile.collectAsState()
            TodoAppTheme(appTheme = userProfile.selectedTheme) {
                // Request notification permission if needed (Android 13+)
                RequestNotificationPermission()
                
                // Main app content
                MainScreen(viewModel = viewModel)
            }
        }
    }
    
    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }
    
    override fun onResume() {
        super.onResume()
        
        // Check for overdue tasks when app resumes
        viewModel.checkForOverdueTasks()
        
        // Start periodic checks
        handler.post(checkOverdueRunnable)
    }
    
    override fun onPause() {
        super.onPause()
        
        // Stop periodic checks when app is paused
        handler.removeCallbacks(checkOverdueRunnable)
    }
    
    private fun handleIntent(intent: android.content.Intent) {
        val action = intent.action
        
        if (action == "com.example.myapplication.OPEN_FROM_NOTIFICATION") {
            val taskId = intent.getStringExtra("taskId")
            if (taskId != null) {
                Log.d(TAG, "Opening from notification. Task ID: $taskId")
                viewModel.highlightTask(taskId)
            }
        }
    }
    
    /**
     * Test notification system - this helps verify the notification capability
     */
    private fun testNotificationSystem() {
        try {
            // Log test info
            Log.d(TAG, "Testing notification system to verify configuration")
            
            // Create dummy test task for notifications
            val dummyTask = com.example.myapplication.data.models.Task(
                title = "Test Task",
                description = "Test notification - please ignore",
                difficulty = com.example.myapplication.data.models.TaskDifficulty.MEDIUM,
                category = com.example.myapplication.data.models.TaskCategory.OTHER
            )
            
            // Test through AlarmScheduler
            val alarmScheduler = com.example.myapplication.notifications.AlarmScheduler(this)
            
            // Check AlarmManager permission for Android 12+
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                val canScheduleAlarms = alarmScheduler.canScheduleExactAlarms()
                Log.d(TAG, "Can schedule exact alarms: $canScheduleAlarms")
            }
            
            // Trigger a test notification directly through NotificationHelper
            val notificationHelper = com.example.myapplication.notifications.NotificationHelper(this)
            val hasPermission = notificationHelper.hasNotificationPermission()
            Log.d(TAG, "Notification permission: $hasPermission")
            
            if (hasPermission) {
                notificationHelper.showTestNotification()
                Log.d(TAG, "Test notification triggered directly")
            } else {
                Log.e(TAG, "Cannot show test notification - no permission")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in test notification: ${e.message}", e)
        }
    }
}