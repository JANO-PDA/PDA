package com.example.myapplication.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import com.example.myapplication.data.models.*
import com.example.myapplication.ui.components.*
import com.example.myapplication.ui.theme.AppIcons
import com.example.myapplication.ui.viewmodel.*
import kotlinx.coroutines.launch
import com.example.myapplication.ui.components.AnimatedBackground
import com.example.myapplication.ui.components.FloatingElement
import com.example.myapplication.ui.components.PulsatingIcon
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: TodoViewModel) {
    val tasks by viewModel.tasks.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val categoryStats by viewModel.categoryStats.collectAsState()
    
    // Track screen state
    var showAddTaskDialog by rememberSaveable { mutableStateOf(false) }
    var showCompletedTasks by rememberSaveable { mutableStateOf(false) }
    var showCategoryProgress by rememberSaveable { mutableStateOf(false) }
    var showContacts by rememberSaveable { mutableStateOf(false) }
    
    // Get state
    val showConfetti by viewModel.showConfetti.collectAsState()
    val npcMessages by viewModel.npcMessages.collectAsState()
    val unreadMessageCount = viewModel.getUnreadMessageCount()
    
    // States
    val addSubtaskFor by viewModel.addSubtaskFor.collectAsState()
    var showAddSubtaskDialog by remember { mutableStateOf(false) }
    
    // Update dialog state
    LaunchedEffect(addSubtaskFor) {
        showAddSubtaskDialog = addSubtaskFor != null
    }
    
    // Prepare task data
    val topLevelTasks = tasks.filter { it.parentTaskId == null }
    val topLevelActiveTasks = topLevelTasks.filter { !it.isCompleted }
    val subtaskMap = tasks.filter { it.parentTaskId != null }
        .groupBy { it.parentTaskId!! }
        
    // Drawer state
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    // Handle screen navigation
    when {
        showCompletedTasks -> {
            CompletedTasksScreen(
                viewModel = viewModel,
                onNavigateBack = { showCompletedTasks = false }
            )
        }
        showCategoryProgress -> {
            CategoryProgressScreen(
                viewModel = viewModel,
                onNavigateBack = { showCategoryProgress = false }
            )
        }
        showContacts -> {
            ContactsScreen(
                viewModel = viewModel,
                onNavigateBack = { showContacts = false }
            )
        }
        else -> {
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    ModalDrawerSheet {
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        // Navigation Items
                        NavigationDrawerItem(
                            icon = { Icon(Icons.Default.Check, contentDescription = "Completed Tasks") },
                            label = { Text("Completed Tasks") },
                            selected = false,
                            onClick = { 
                                scope.launch { 
                                    drawerState.close()
                                    showCompletedTasks = true
                                }
                            },
                            badge = {
                                val completedCount = tasks.count { it.isCompleted && it.parentTaskId == null }
                                if (completedCount > 0) {
                                    Badge { Text(completedCount.toString()) }
                                }
                            },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                        
                        NavigationDrawerItem(
                            icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Category Progress") },
                            label = { Text("Category Progress") },
                            selected = false,
                            onClick = { 
                                scope.launch { 
                                    drawerState.close()
                                    showCategoryProgress = true 
                                } 
                            },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }
                }
            ) {
                // Use animated background
                AnimatedBackground {
                    // Add floating icons in the background
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Add several pulsating icons at random positions
                        repeat(5) { index ->
                            val random = Random(index)
                            val xPosition = random.nextInt(20, 80)
                            val yPosition = random.nextInt(10, 90)
                            val iconSize = random.nextInt(16, 25)
                            
                            // Choose icons based on index
                            val icon = when(index % 3) {
                                0 -> Icons.Default.Star
                                1 -> Icons.Default.CheckCircle
                                else -> AppIcons.getRankIcon(getCategoryRankLevel(index+1))
                            }
                            
                            // Create a floating pulsating icon
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .offset(
                                        x = (xPosition * 0.01f * 100).dp, 
                                        y = (yPosition * 0.01f * 100).dp
                                    )
                            ) {
                                FloatingElement(
                                    floatHeight = random.nextFloat() * 20f + 5f,
                                    floatDuration = random.nextInt(2000, 5000)
                                ) {
                                    PulsatingIcon(
                                        icon = icon,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                        rotateEnabled = random.nextBoolean(),
                                        pulseEnabled = true,
                                        pulseMinScale = 0.8f,
                                        pulseMaxScale = 1.2f,
                                        pulseDurationMs = random.nextInt(1000, 3000),
                                        modifier = Modifier.size(iconSize.dp)
                                    )
                                }
                            }
                        }
                        
                        // Main content with the existing Scaffold
                        Scaffold(
                            topBar = {
                                TopAppBar(
                                    title = { Text("Tasks", Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                                    navigationIcon = {
                                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                            Icon(
                                                imageVector = Icons.Default.Menu,
                                                contentDescription = "Open menu"
                                            )
                                        }
                                    },
                                    actions = {
                                        // Contacts button with badge for unread messages
                                        IconButton(onClick = { showContacts = true }) {
                                            BadgedBox(
                                                badge = {
                                                    if (unreadMessageCount > 0) {
                                                        Badge { Text(unreadMessageCount.toString()) }
                                                    }
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Forum,
                                                    contentDescription = "Contacts"
                                                )
                                            }
                                        }
                                    }
                                )
                            },
                            floatingActionButton = {
                                FloatingActionButton(onClick = { showAddTaskDialog = true }) {
                                    Icon(Icons.Default.Add, contentDescription = "Add Task")
                                }
                            }
                        ) { padding ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(padding)
                                    .pointerInput(Unit) {
                                        detectHorizontalDragGestures { _, dragAmount ->
                                            // Detect right swipe (positive dragAmount)
                                            if (dragAmount > 10) {
                                                // Open drawer on right swipe
                                                scope.launch { drawerState.open() }
                                            }
                                        }
                                    }
                            ) {
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // User Profile Card
                                    item {
                                        UserProfileCard(
                                            userProfile = userProfile,
                                            modifier = Modifier.padding(vertical = 16.dp)
                                        )
                                    }
                                    
                                    // Tasks header
                                    item {
                                        Text(
                                            text = "Active Tasks",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(vertical = 8.dp)
                                        )
                                    }
                                    
                                    // Task items
                                    items(topLevelActiveTasks) { task ->
                                        val taskSubtasks = subtaskMap[task.id] ?: emptyList()
                                        val highlightedId by viewModel.highlightedTaskId.collectAsState()
                                        val isHighlighted = task.id == highlightedId
                                        
                                        TaskItem(
                                            task = task,
                                            onComplete = { viewModel.completeTask(task) },
                                            onDelete = { viewModel.deleteTask(task) },
                                            subtasks = taskSubtasks.filter { !it.isCompleted },
                                            onAddSubtask = { viewModel.showAddSubtaskDialog(it) },
                                            onCompleteSubtask = { viewModel.completeSubtask(it) },
                                            isHighlighted = isHighlighted
                                        )
                                    }
                                    
                                    // Empty state
                                    item {
                                        if (topLevelActiveTasks.isEmpty()) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 32.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text(
                                                    text = "No active tasks",
                                                    style = MaterialTheme.typography.titleLarge
                                                )
                                                
                                                Spacer(modifier = Modifier.height(8.dp))
                                                
                                                Text(
                                                    text = "Tap the + button to add a new task",
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                        }
                                    }
                                    
                                    // Add only alarm permission fix if needed
                                    item {
                                        // Check if alarm permission is needed
                                        val hasAlarmPerm = viewModel.checkAlarmPermissions()
                                        
                                        // Show fix button only if permission is missing
                                        if (!hasAlarmPerm) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(bottom = 16.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text(
                                                    text = "⚠️ Reminders won't work without alarm permission",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.error,
                                                    textAlign = TextAlign.Center,
                                                    modifier = Modifier.padding(bottom = 8.dp)
                                                )
                                                
                                                Button(
                                                    onClick = { viewModel.openAlarmPermissionSettings() },
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = MaterialTheme.colorScheme.errorContainer,
                                                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                                                    )
                                                ) {
                                                    Text("Fix Alarm Permission")
                                                }
                                                
                                                Text(
                                                    text = "1. Tap 'Fix Alarm Permission'\n" +
                                                          "2. Find this app in the list\n" +
                                                          "3. Enable 'Use exact alarms'",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    textAlign = TextAlign.Center,
                                                    modifier = Modifier.padding(top = 8.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Dialogs
    if (showAddTaskDialog) {
        AddTaskDialog(
            onDismiss = { showAddTaskDialog = false },
            onAddTask = { title, description, difficulty, category, dueDate, dueTime, hasReminder ->
                viewModel.addTask(title, description, difficulty, category, dueDate, dueTime, hasReminder)
                showAddTaskDialog = false
            }
        )
    }
    
    if (showAddSubtaskDialog && addSubtaskFor != null) {
        AddSubtaskDialog(
            parentTask = addSubtaskFor!!,
            onDismiss = { viewModel.dismissAddSubtaskDialog() },
            onAddSubtask = {
                viewModel.addSubtask(
                    parentTaskId = addSubtaskFor!!.id,
                    title = subtaskTitleValue,
                    description = subtaskDescriptionValue,
                    difficulty = subtaskDifficultyValue
                )
                viewModel.dismissAddSubtaskDialog()
            }
        )
    }
}

private fun getThemeDescription(theme: AppTheme): String {
    return when (theme) {
        AppTheme.ZONE_EXPLORER -> "A green theme inspired by the Zone's vegetation"
        AppTheme.RADIATION -> "An orange theme representing radiation and danger"
        AppTheme.PRIPYAT -> "A blue theme reflecting the cold atmosphere of Pripyat"
    }
} 