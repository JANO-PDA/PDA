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
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
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
    var showContactsScreen by rememberSaveable { mutableStateOf(false) }
    
    // Get state
    val showConfetti by viewModel.showConfetti.collectAsState()
    val npcMessages by viewModel.npcMessages.collectAsState()
    
    // States
    val addSubtaskFor by viewModel.addSubtaskFor.collectAsState()
    var showAddSubtaskDialog by remember { mutableStateOf(false) }
    
    // Drawer state
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    // Update dialog state
    LaunchedEffect(addSubtaskFor) {
        showAddSubtaskDialog = addSubtaskFor != null
    }
    
    // Handle screen navigation
    if (showContactsScreen) {
        ContactsScreen(
            viewModel = viewModel,
            onNavigateBack = { showContactsScreen = false }
        )
        return
    }
    
    if (showCompletedTasks) {
        CompletedTasksScreen(
            viewModel = viewModel,
            onNavigateBack = { showCompletedTasks = false }
        )
        return
    }
    
    if (showCategoryProgress) {
        CategoryProgressScreen(
            viewModel = viewModel,
            onNavigateBack = { showCategoryProgress = false }
        )
        return
    }
    
    // Prepare task data
    val topLevelTasks = tasks.filter { it.parentTaskId == null }
    val topLevelActiveTasks = topLevelTasks.filter { !it.isCompleted }
    val subtaskMap = tasks.filter { it.parentTaskId != null }
        .groupBy { it.parentTaskId!! }
    
    // Navigation Drawer
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(modifier = Modifier.height(24.dp))
                
                // Header
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "PDA Menu",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                
                // Navigation Items
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.CheckCircle, contentDescription = "Completed Tasks") },
                    label = { Text("Completed Tasks") },
                    badge = {
                        val completedCount = tasks.count { it.isCompleted }
                        if (completedCount > 0) {
                            Badge { Text(completedCount.toString()) }
                        }
                    },
                    selected = false,
                    onClick = { 
                        scope.launch {
                            drawerState.close()
                            showCompletedTasks = true
                        }
                    },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
                
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Analytics, contentDescription = "Category Statistics") },
                    label = { Text("Category Statistics") },
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
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        // Message icon with badge
                        IconButton(onClick = { showContactsScreen = true }) {
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