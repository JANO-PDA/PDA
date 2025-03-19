package com.example.myapplication.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.models.Task
import com.example.myapplication.data.models.TaskDifficulty
import com.example.myapplication.ui.viewmodel.subtaskTitleValue
import com.example.myapplication.ui.viewmodel.subtaskDescriptionValue
import com.example.myapplication.ui.viewmodel.subtaskDifficultyValue

@Composable
fun AddSubtaskDialog(
    parentTask: Task,
    onDismiss: () -> Unit,
    onAddSubtask: () -> Unit
) {
    var title by remember { mutableStateOf(subtaskTitleValue) }
    var description by remember { mutableStateOf(subtaskDescriptionValue) }
    var selectedDifficulty by remember { mutableStateOf(subtaskDifficultyValue) }
    
    // Update the shared values when the local state changes
    LaunchedEffect(title) {
        com.example.myapplication.ui.viewmodel.subtaskTitleValue = title
    }
    
    LaunchedEffect(description) {
        com.example.myapplication.ui.viewmodel.subtaskDescriptionValue = description
    }
    
    LaunchedEffect(selectedDifficulty) {
        com.example.myapplication.ui.viewmodel.subtaskDifficultyValue = selectedDifficulty
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Subtask") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Parent Task: ${parentTask.title}", style = MaterialTheme.typography.bodyMedium)
                
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Subtask Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
                
                Text("Difficulty", style = MaterialTheme.typography.titleSmall)
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(TaskDifficulty.values()) { difficulty ->
                        FilterChip(
                            selected = selectedDifficulty == difficulty,
                            onClick = { selectedDifficulty = difficulty },
                            label = { 
                                Text(
                                    text = difficulty.name,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    if (title.isNotBlank()) {
                        onAddSubtask()
                    }
                },
                enabled = title.isNotBlank()
            ) {
                Text("Add Subtask")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
} 