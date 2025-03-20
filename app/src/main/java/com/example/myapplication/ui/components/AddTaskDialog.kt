package com.example.myapplication.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.models.TaskCategory
import com.example.myapplication.data.models.TaskDifficulty
import com.example.myapplication.ui.theme.AppIcons
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onAddTask: (String, String, TaskDifficulty, TaskCategory, LocalDate?, LocalTime?, Boolean) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedDifficulty by remember { mutableStateOf(TaskDifficulty.MEDIUM) }
    var selectedCategory by remember { mutableStateOf(TaskCategory.PERSONAL) }
    var hasDueDate by remember { mutableStateOf(false) }
    
    // Default due date: tomorrow
    var selectedDate by remember { mutableStateOf(LocalDate.now().plusDays(1)) }
    var selectedTime by remember { mutableStateOf(LocalTime.of(9, 0)) } // Default 9:00 AM
    var hasReminder by remember { mutableStateOf(false) }
    
    // Dialog states
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    
    // Format for displaying dates and times
    val dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Task") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    placeholder = { Text("Enter task title") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    placeholder = { Text("Enter task description (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = hasDueDate,
                        onCheckedChange = { hasDueDate = it }
                    )
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Due Date",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Set due date", style = MaterialTheme.typography.bodyMedium)
                }
                
                // Due date and time pickers
                if (hasDueDate) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Date picker button
                        OutlinedButton(
                            onClick = { showDatePicker = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = "Date",
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(selectedDate.format(dateFormatter))
                            }
                        }
                        
                        // Time picker button
                        OutlinedButton(
                            onClick = { showTimePicker = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccessTime,
                                    contentDescription = "Time",
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(selectedTime.format(timeFormatter))
                            }
                        }
                    }
                    
                    // Reminder option
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = hasReminder,
                            onCheckedChange = { hasReminder = it },
                            enabled = hasDueDate
                        )
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Reminder",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Set reminder", style = MaterialTheme.typography.bodyMedium)
                    }
                }
                
                Text("Difficulty", style = MaterialTheme.typography.titleSmall)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // First row of difficulties (EASY and MEDIUM)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // EASY difficulty
                        FilterChip(
                            selected = selectedDifficulty == TaskDifficulty.EASY,
                            onClick = { selectedDifficulty = TaskDifficulty.EASY },
                            label = { Text(text = "EASY") },
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        // MEDIUM difficulty
                        FilterChip(
                            selected = selectedDifficulty == TaskDifficulty.MEDIUM,
                            onClick = { selectedDifficulty = TaskDifficulty.MEDIUM },
                            label = { Text(text = "MEDIUM") },
                            modifier = Modifier.weight(1f, fill = false)
                        )
                    }
                    
                    // Second row of difficulties (HARD and NIGHTMARE)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // HARD difficulty
                        FilterChip(
                            selected = selectedDifficulty == TaskDifficulty.HARD,
                            onClick = { selectedDifficulty = TaskDifficulty.HARD },
                            label = { Text(text = "HARD") },
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        // NIGHTMARE difficulty
                        FilterChip(
                            selected = selectedDifficulty == TaskDifficulty.NIGHTMARE,
                            onClick = { selectedDifficulty = TaskDifficulty.NIGHTMARE },
                            label = { Text(text = "NIGHTMARE") },
                            modifier = Modifier.weight(1f, fill = false)
                        )
                    }
                }
                
                Text("Category", style = MaterialTheme.typography.titleSmall)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // First row of categories
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // WORK category
                        FilterChip(
                            selected = selectedCategory == TaskCategory.WORK,
                            onClick = { selectedCategory = TaskCategory.WORK },
                            label = { 
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = AppIcons.getCategoryIcon(TaskCategory.WORK),
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "WORK")
                                }
                            },
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        // STUDY category
                        FilterChip(
                            selected = selectedCategory == TaskCategory.STUDY,
                            onClick = { selectedCategory = TaskCategory.STUDY },
                            label = { 
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = AppIcons.getCategoryIcon(TaskCategory.STUDY),
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "STUDY")
                                }
                            },
                            modifier = Modifier.weight(1f, fill = false)
                        )
                    }
                    
                    // Second row of categories
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // HEALTH category
                        FilterChip(
                            selected = selectedCategory == TaskCategory.HEALTH,
                            onClick = { selectedCategory = TaskCategory.HEALTH },
                            label = { 
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = AppIcons.getCategoryIcon(TaskCategory.HEALTH),
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "HEALTH")
                                }
                            },
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        // PERSONAL category
                        FilterChip(
                            selected = selectedCategory == TaskCategory.PERSONAL,
                            onClick = { selectedCategory = TaskCategory.PERSONAL },
                            label = { 
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = AppIcons.getCategoryIcon(TaskCategory.PERSONAL),
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "PERSONAL")
                                }
                            },
                            modifier = Modifier.weight(1f, fill = false)
                        )
                    }
                    
                    // Third row of categories
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // SHOPPING category
                        FilterChip(
                            selected = selectedCategory == TaskCategory.SHOPPING,
                            onClick = { selectedCategory = TaskCategory.SHOPPING },
                            label = { 
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = AppIcons.getCategoryIcon(TaskCategory.SHOPPING),
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "SHOPPING")
                                }
                            },
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        // OTHER category
                        FilterChip(
                            selected = selectedCategory == TaskCategory.OTHER,
                            onClick = { selectedCategory = TaskCategory.OTHER },
                            label = { 
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = AppIcons.getCategoryIcon(TaskCategory.OTHER),
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "OTHER")
                                }
                            },
                            modifier = Modifier.weight(1f, fill = false)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isNotBlank()) {
                        onAddTask(
                            title, 
                            description, 
                            selectedDifficulty, 
                            selectedCategory,
                            if (hasDueDate) selectedDate else null,
                            if (hasDueDate) selectedTime else null,
                            hasReminder && hasDueDate
                        )
                        onDismiss()
                    }
                },
                enabled = title.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
    
    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val newDate = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                            selectedDate = newDate
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    
    // Time Picker Dialog
    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = selectedTime.hour,
            initialMinute = selectedTime.minute
        )
        
        TimePickerDialog(
            onDismiss = { showTimePicker = false },
            onConfirm = {
                selectedTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                showTimePicker = false
            }
        ) {
            TimePicker(state = timePickerState)
        }
    }
} 