package com.example.myapplication.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Message
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
import java.util.Date
import kotlin.comparisons.compareByDescending

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
    var showMenuDrawer by rememberSaveable { mutableStateOf(false) }
    var showMessagesDialog by rememberSaveable { mutableStateOf(false) }
    
    // Get state
    val showConfetti by viewModel.showConfetti.collectAsState()
    val npcMessages by viewModel.npcMessages.collectAsState()
    
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
    
    // Main content
    Scaffold(
        topBar = {
            // Top bar with simple title
            TopAppBar(
                title = { Text("Tasks") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                navigationIcon = {
                    IconButton(onClick = { showMenuDrawer = true }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                },
                actions = {
                    // Message icon with badge
                    IconButton(onClick = { showMessagesDialog = true }) {
                        BadgedBox(
                            badge = {
                                if (viewModel.getUnreadMessageCount() > 0) {
                                    Badge { Text(viewModel.getUnreadMessageCount().toString()) }
                                }
                            }
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Message, contentDescription = "Messages")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            // Floating action button for adding tasks
            FloatingActionButton(
                onClick = { showAddTaskDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Task")
            }
        }
    ) { paddingValues ->
        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            // User Profile Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Header with icon
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "User Profile",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.requiredSize(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "User Profile",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Level and XP
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Level:",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = userProfile.level.toString(),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Total XP:",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = userProfile.totalXp.toString(),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // XP Progress section
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "XP Progress",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.requiredSize(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "XP Progress",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Progress bar
                    val progress = calculateProgressToNextLevel(userProfile.totalXp)
                    val xpForNextLevel = calculateXpForNextLevel(userProfile.totalXp)
                    val xpNeeded = xpForNextLevel - userProfile.totalXp
                    
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primaryContainer
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // XP to next level
                    Text(
                        text = "$xpNeeded XP to Level ${userProfile.level + 1}",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "XP to next level: $xpNeeded",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Active Tasks section
            Text(
                text = "Active Tasks",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Task list
            if (topLevelActiveTasks.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "No active tasks",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tap the + button to add a new task",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                // Task list with tasks
                TaskList(
                    tasks = topLevelActiveTasks,
                    subtaskMap = subtaskMap,
                    onTaskClick = { task ->
                        viewModel.highlightTask(task.id)
                    },
                    onTaskComplete = { task ->
                        viewModel.completeTask(task)
                    },
                    onAddSubtask = { task ->
                        viewModel.setAddSubtaskFor(task)
                    },
                    viewModel = viewModel
                )
            }
        }
    }
    
    // Menu Drawer
    if (showMenuDrawer) {
        AlertDialog(
            onDismissRequest = { showMenuDrawer = false },
            title = { Text("Menu") },
            text = {
                Column {
                    Button(
                        onClick = { 
                            showCompletedTasks = !showCompletedTasks
                            showMenuDrawer = false 
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (showCompletedTasks) "Hide Completed Tasks" else "Show Completed Tasks")
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Button(
                        onClick = { 
                            showCategoryProgress = true
                            showMenuDrawer = false 
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Category Statistics")
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Button(
                        onClick = { 
                            viewModel.deleteAllCompletedTasks()
                            showMenuDrawer = false 
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Clear Completed Tasks")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showMenuDrawer = false }) {
                    Text("Close")
                }
            }
        )
    }
    
    // NPC Messages Dialog
    if (showMessagesDialog) {
        AlertDialog(
            onDismissRequest = { showMessagesDialog = false },
            title = { 
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Message,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("NPC Messages")
                }
            },
            text = {
                // Access the state value differently to avoid ambiguity
                val messagesList = (npcMessages as State<List<NpcMessage>>).value
                if (messagesList.isEmpty()) {
                    Text(
                        text = "No messages yet",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    // Use sortedByDescending with direct access to timestamp
                    val sortedMessages = messagesList.sortedByDescending { it.timestamp.time }
                    
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 400.dp)
                    ) {
                        items(sortedMessages) { msg ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { 
                                        viewModel.markMessageAsRead(msg.id)
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (msg.isRead) 
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                                    else 
                                        MaterialTheme.colorScheme.primaryContainer
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = msg.npcName,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = formatTimestamp(msg.timestamp.time),
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.height(4.dp))
                                    
                                    Text(
                                        text = msg.message,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    
                                    if (!msg.isRead) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "NEW",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.align(Alignment.End)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { 
                        viewModel.markAllMessagesAsRead()
                        showMessagesDialog = false
                    }
                ) {
                    Text("Mark All Read")
                }
            },
            dismissButton = {
                TextButton(onClick = { showMessagesDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
    
    // Regular Dialogs
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
            onAddSubtask = { subtaskTitleValue, subtaskDescriptionValue, subtaskDifficultyValue ->
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
    
    // Category Progress Dialog
    if (showCategoryProgress) {
        AlertDialog(
            onDismissRequest = { showCategoryProgress = false },
            title = { 
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Analytics,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Category Progress")
                }
            },
            text = {
                LazyColumn {
                    items(categoryStats.toList()) { (category, stats) ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp)
                            ) {
                                Text(
                                    text = category.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                
                                Spacer(modifier = Modifier.height(4.dp))
                                
                                LinearProgressIndicator(
                                    progress = { stats.completionRate },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                
                                Spacer(modifier = Modifier.height(4.dp))
                                
                                Text(
                                    text = "${stats.completedTasks}/${stats.totalTasks} tasks completed (${(stats.completionRate * 100).toInt()}%)",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCategoryProgress = false }) {
                    Text("Close")
                }
            }
        )
    }
}

// Helper function to format timestamps
private fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diffMillis = now - timestamp
    
    return when {
        diffMillis < 60000 -> "Just now"
        diffMillis < 3600000 -> "${(diffMillis / 60000).toInt()}m ago"
        diffMillis < 86400000 -> "${(diffMillis / 3600000).toInt()}h ago"
        else -> "${(diffMillis / 86400000).toInt()}d ago"
    }
}

private fun getThemeDescription(theme: AppTheme): String {
    return when (theme) {
        AppTheme.ZONE_EXPLORER -> "A green theme inspired by the Zone's vegetation"
        AppTheme.RADIATION -> "An orange theme representing radiation and danger"
        AppTheme.PRIPYAT -> "A blue theme reflecting the cold atmosphere of Pripyat"
    }
} 