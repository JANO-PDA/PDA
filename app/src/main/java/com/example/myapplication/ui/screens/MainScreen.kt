package com.example.myapplication.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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

// Fix 7: Sort order enum
enum class SortOrder(val label: String) {
    DEFAULT("Default"),
    DUE_DATE("Due Date"),
    DIFFICULTY("Difficulty")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: TodoViewModel) {
    val tasks by viewModel.tasks.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val categoryStats by viewModel.categoryStats.collectAsState()

    var showAddTaskDialog by rememberSaveable { mutableStateOf(false) }
    var showCompletedTasks by rememberSaveable { mutableStateOf(false) }
    var showCategoryProgress by rememberSaveable { mutableStateOf(false) }
    var showContactsScreen by rememberSaveable { mutableStateOf(false) }
    var showSettingsScreen by rememberSaveable { mutableStateOf(false) }

    val showConfetti by viewModel.showConfetti.collectAsState()
    val npcMessages by viewModel.npcMessages.collectAsState()

    val addSubtaskFor by viewModel.addSubtaskFor.collectAsState()
    var showAddSubtaskDialog by remember { mutableStateOf(false) }

    // Fix 6: Category filter state
    var selectedCategory by remember { mutableStateOf<TaskCategory?>(null) }

    // Fix 7: Sort order state
    var sortOrder by remember { mutableStateOf(SortOrder.DEFAULT) }
    var showSortMenu by remember { mutableStateOf(false) }

    // Fix 9: Task editing state
    var editingTask by remember { mutableStateOf<Task?>(null) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val isDrawerOpen = remember { derivedStateOf { drawerState.currentValue == DrawerValue.Open } }

    LaunchedEffect(addSubtaskFor) {
        showAddSubtaskDialog = addSubtaskFor != null
    }

    if (showContactsScreen) {
        ContactsScreen(viewModel = viewModel, onNavigateBack = { showContactsScreen = false })
        return
    }
    if (showCompletedTasks) {
        CompletedTasksScreen(viewModel = viewModel, onNavigateBack = { showCompletedTasks = false })
        return
    }
    if (showCategoryProgress) {
        CategoryProgressScreen(viewModel = viewModel, onNavigateBack = { showCategoryProgress = false })
        return
    }
    if (showSettingsScreen) {
        SettingsScreen(viewModel = viewModel, onNavigateBack = { showSettingsScreen = false })
        return
    }

    val topLevelTasks = tasks.filter { it.parentTaskId == null }
    val topLevelActiveTasks = topLevelTasks.filter { !it.isCompleted }
    val subtaskMap = tasks.filter { it.parentTaskId != null }.groupBy { it.parentTaskId!! }

    // Fix 6: apply category filter
    val filteredTasks = if (selectedCategory != null)
        topLevelActiveTasks.filter { it.category == selectedCategory }
    else
        topLevelActiveTasks

    // Fix 7: apply sort order
    val displayTasks = when (sortOrder) {
        SortOrder.DEFAULT    -> filteredTasks
        SortOrder.DUE_DATE   -> filteredTasks.sortedWith(compareBy(nullsLast()) { it.dueDate })
        SortOrder.DIFFICULTY -> filteredTasks.sortedByDescending { it.difficulty.ordinal }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(modifier = Modifier.height(24.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "PDA Menu",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.CheckCircle, contentDescription = "Completed Tasks") },
                    label = { Text("Completed Tasks") },
                    badge = {
                        val completedCount = tasks.count { it.isCompleted }
                        if (completedCount > 0) Badge { Text(completedCount.toString()) }
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

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") },
                    selected = false,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            showSettingsScreen = true
                        }
                    },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        }
    ) {
        // Fix 5: AnimatedBackground wraps the entire main content area
        AnimatedBackground {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Tasks") },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                            titleContentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        navigationIcon = {
                            if (!isDrawerOpen.value) {
                                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                    Icon(Icons.Default.Menu, contentDescription = "Menu")
                                }
                            }
                        },
                        actions = {
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
                floatingActionButton = { },
                containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0f) // transparent so AnimatedBackground shows
            ) { paddingValues ->
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
                                Text(text = "User Profile", style = MaterialTheme.typography.titleLarge)
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Level:", style = MaterialTheme.typography.bodyLarge)
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
                                Text(text = "Total XP:", style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    text = userProfile.totalXp.toString(),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Streak display
                            if (userProfile.taskStreak > 0) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "Streak:", style = MaterialTheme.typography.bodyLarge)
                                    Text(
                                        text = "${userProfile.taskStreak} day${if (userProfile.taskStreak != 1) "s" else ""}",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.tertiary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

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
                                Text(text = "XP Progress", style = MaterialTheme.typography.titleMedium)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            val progress = calculateProgressToNextLevel(userProfile.totalXp)
                            val xpForNextLevel = calculateXpForNextLevel(userProfile.totalXp)
                            val xpNeeded = xpForNextLevel - userProfile.totalXp

                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier.fillMaxWidth().height(8.dp),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.primaryContainer
                            )

                            Spacer(modifier = Modifier.height(8.dp))

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

                    // Active Tasks header with Add + Sort buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Active Tasks",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Fix 7: Sort button with dropdown
                            Box {
                                IconButton(onClick = { showSortMenu = true }) {
                                    Icon(Icons.Default.Sort, contentDescription = "Sort tasks")
                                }
                                DropdownMenu(
                                    expanded = showSortMenu,
                                    onDismissRequest = { showSortMenu = false }
                                ) {
                                    SortOrder.values().forEach { order ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = order.label,
                                                    fontWeight = if (sortOrder == order) FontWeight.Bold else FontWeight.Normal
                                                )
                                            },
                                            onClick = {
                                                sortOrder = order
                                                showSortMenu = false
                                            }
                                        )
                                    }
                                }
                            }

                            Button(
                                onClick = { showAddTaskDialog = true },
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add Task", modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Task")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Fix 6: Category filter chips (horizontally scrollable)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedCategory == null,
                            onClick = { selectedCategory = null },
                            label = { Text("All") }
                        )
                        TaskCategory.values().forEach { category ->
                            FilterChip(
                                selected = selectedCategory == category,
                                onClick = {
                                    selectedCategory = if (selectedCategory == category) null else category
                                },
                                label = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = AppIcons.getCategoryIcon(category),
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(category.name.lowercase().replaceFirstChar { it.uppercase() })
                                    }
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Task list or empty state
                    if (displayTasks.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = if (selectedCategory != null) "No ${selectedCategory!!.name.lowercase()} tasks" else "No active tasks",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Tap the Add Task button to add a new task",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }
                    } else {
                        TaskList(
                            tasks = displayTasks,
                            subtaskMap = subtaskMap,
                            onTaskClick = { task -> viewModel.highlightTask(task.id) },
                            onTaskComplete = { task -> viewModel.completeTask(task) },
                            onAddSubtask = { task -> viewModel.setAddSubtaskFor(task) },
                            onTaskEdit = { task -> editingTask = task },  // Fix 9
                            viewModel = viewModel
                        )
                    }
                }
            }

            // Fix 3: Confetti animation re-enabled
            if (showConfetti) {
                ConfettiAnimation(numConfetti = 100)
            }
        }
    }

    // Add task dialog
    if (showAddTaskDialog) {
        AddTaskDialog(
            onDismiss = { showAddTaskDialog = false },
            onAddTask = { title, description, difficulty, category, dueDate, dueTime, hasReminder ->
                viewModel.addTask(title, description, difficulty, category, dueDate, dueTime, hasReminder)
                showAddTaskDialog = false
            }
        )
    }

    // Fix 9: Edit task dialog
    editingTask?.let { task ->
        AddTaskDialog(
            existingTask = task,
            onDismiss = { editingTask = null },
            onAddTask = { title, description, difficulty, category, dueDate, dueTime, hasReminder ->
                viewModel.updateTask(
                    task.copy(
                        title = title,
                        description = description,
                        difficulty = difficulty,
                        category = category,
                        dueDate = dueDate,
                        dueTime = dueTime,
                        hasReminder = hasReminder
                    )
                )
                editingTask = null
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

private fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diffMillis = now - timestamp
    return when {
        diffMillis < 60000   -> "Just now"
        diffMillis < 3600000 -> "${(diffMillis / 60000).toInt()}m ago"
        diffMillis < 86400000 -> "${(diffMillis / 3600000).toInt()}h ago"
        else                 -> "${(diffMillis / 86400000).toInt()}d ago"
    }
}

private fun getThemeDescription(theme: AppTheme): String {
    return when (theme) {
        AppTheme.ZONE_EXPLORER -> "A green theme inspired by the Zone's vegetation"
        AppTheme.RADIATION     -> "An orange theme representing radiation and danger"
        AppTheme.PRIPYAT       -> "A blue theme reflecting the cold atmosphere of Pripyat"
    }
}
