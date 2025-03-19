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
            // Schedule the next check in 1 second (instead of 1 minute)
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize ViewModel with context for sound playback
        viewModel.initialize(this)
        
        // Handle notification intent if present
        handleIntent(intent)
        
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
}