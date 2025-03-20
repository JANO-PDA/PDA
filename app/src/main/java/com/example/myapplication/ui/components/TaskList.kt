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
    var selectedParentTask by remember { mutableStateOf<Task?>(null) }
    
    val highlightedTaskId by viewModel?.highlightedTaskId?.collectAsState() ?: remember { mutableStateOf(null) }
    
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Task items
        items(
            items = tasks,
            key = { it.id }
        ) { task ->
            val taskSubtasks = subtaskMap[task.id] ?: emptyList()
            val isHighlighted = task.id == highlightedTaskId
            
            TaskItem(
                task = task,
                onComplete = { onTaskComplete(task) },
                onDelete = { viewModel?.deleteTask(task) },
                subtasks = taskSubtasks.filter { !it.isCompleted },
                onAddSubtask = { selectedParentTask = it },
                onCompleteSubtask = { subtask -> 
                    viewModel?.completeTask(subtask)
                },
                isHighlighted = isHighlighted,
                modifier = Modifier.padding(vertical = 8.dp)
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