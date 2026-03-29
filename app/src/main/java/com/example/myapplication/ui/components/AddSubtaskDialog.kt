package com.example.myapplication.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.models.Task
import com.example.myapplication.data.models.TaskDifficulty

@Composable
fun AddSubtaskDialog(
    parentTask: Task,
    onDismiss: () -> Unit,
    onAddSubtask: (String, String, TaskDifficulty) -> Unit
) {
    var title             by remember { mutableStateOf("") }
    var description       by remember { mutableStateOf("") }
    var selectedDifficulty by remember { mutableStateOf(TaskDifficulty.MEDIUM) }

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
                Text("Parent: ${parentTask.title}", style = MaterialTheme.typography.bodyMedium)

                OutlinedTextField(
                    value       = title,
                    onValueChange = { title = it },
                    label       = { Text("Subtask Title") },
                    modifier    = Modifier.fillMaxWidth(),
                    singleLine  = true
                )

                OutlinedTextField(
                    value       = description,
                    onValueChange = { description = it },
                    label       = { Text("Description (Optional)") },
                    modifier    = Modifier.fillMaxWidth(),
                    minLines    = 2
                )

                Text("Difficulty", style = MaterialTheme.typography.labelLarge)
                LazyColumn(
                    modifier       = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(TaskDifficulty.entries) { difficulty ->
                        FilterChip(
                            selected = selectedDifficulty == difficulty,
                            onClick  = { selectedDifficulty = difficulty },
                            label    = {
                                Text(
                                    text     = difficulty.name,
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
                onClick  = { if (title.isNotBlank()) onAddSubtask(title, description, selectedDifficulty) },
                enabled  = title.isNotBlank()
            ) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
