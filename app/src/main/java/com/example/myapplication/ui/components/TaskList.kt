package com.example.myapplication.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import com.example.myapplication.data.models.Task
import com.example.myapplication.data.models.TaskCategory
import com.example.myapplication.data.models.TaskDifficulty
import com.example.myapplication.ui.screens.CompletedTasksScreen
import com.example.myapplication.ui.screens.CategoryProgressScreen
import com.example.myapplication.ui.screens.ContactsScreen
import com.example.myapplication.ui.viewmodel.TodoViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskList(
    tasks: List<Task>,
    subtaskMap: Map<String, List<Task>>,
    onTaskClick: (Task) -> Unit,
    onTaskComplete: (Task) -> Unit,
    onAddSubtask: (Task) -> Unit,
    viewModel: TodoViewModel? = null,
    modifier: Modifier = Modifier
) {
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var showCompletedTasks by remember { mutableStateOf(false) }
    var showCategoryProgress by remember { mutableStateOf(false) }
    var showContacts by remember { mutableStateOf(false) }
    var selectedParentTask by remember { mutableStateOf<Task?>(null) }
    
    // Drawer state
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    when {
        showCompletedTasks && viewModel != null -> {
            CompletedTasksScreen(
                viewModel = viewModel,
                onNavigateBack = { showCompletedTasks = false }
            )
        }
        showCategoryProgress && viewModel != null -> {
            CategoryProgressScreen(
                viewModel = viewModel,
                onNavigateBack = { showCategoryProgress = false }
            )
        }
        showContacts && viewModel != null -> {
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
                                val completedCount = tasks.count { it.isCompleted }
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
                                IconButton(onClick = { showContacts = true }) {
                                    Icon(
                                        imageVector = Icons.Default.Forum,
                                        contentDescription = "Contacts"
                                    )
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
                                    if (dragAmount > 10) {
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
                            items(tasks) { task ->
                                val taskSubtasks = subtaskMap[task.id] ?: emptyList()
                                val highlightedTaskId = viewModel?.highlightedTaskId?.collectAsState()?.value
                                val isHighlighted = task.id == highlightedTaskId
                                
                                TaskItem(
                                    task = task,
                                    onComplete = { onTaskComplete(task) },
                                    onDelete = { /* Handle delete */ },
                                    subtasks = taskSubtasks.filter { !it.isCompleted },
                                    onAddSubtask = { selectedParentTask = it },
                                    onCompleteSubtask = { subtask -> 
                                        viewModel?.completeTask(subtask)
                                    },
                                    isHighlighted = isHighlighted
                                )
                            }
                            
                            // Empty state
                            item {
                                if (tasks.isEmpty()) {
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
                        }
                    }
                }
            }
        }
    }
    
    // Add Task Dialog
    if (showAddTaskDialog) {
        AddTaskDialog(
            onDismiss = { showAddTaskDialog = false },
            onAddTask = { title, description, difficulty, category, dueDate, dueTime, hasReminder ->
                // Call viewModel's addTask method if viewModel is not null
                viewModel?.addTask(
                    title = title,
                    description = description,
                    difficulty = difficulty,
                    category = category,
                    dueDate = dueDate,
                    dueTime = dueTime,
                    hasReminder = hasReminder
                )
                showAddTaskDialog = false
            }
        )
    }
    
    // Add Subtask Dialog
    selectedParentTask?.let { parentTask ->
        AddSubtaskDialog(
            parentTask = parentTask,
            onDismiss = { selectedParentTask = null },
            onAddSubtask = { title, description, difficulty ->
                viewModel?.addSubtask(
                    parentTaskId = parentTask.id,
                    title = title,
                    description = description,
                    difficulty = difficulty
                )
                selectedParentTask = null
            }
        )
    }
} 