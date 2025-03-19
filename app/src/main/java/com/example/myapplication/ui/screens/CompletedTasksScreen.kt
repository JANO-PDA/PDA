package com.example.myapplication.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.activity.compose.BackHandler
import com.example.myapplication.data.models.Task
import com.example.myapplication.data.models.TaskCategory
import com.example.myapplication.data.models.UserProfile
import com.example.myapplication.ui.components.TaskItem
import com.example.myapplication.ui.viewmodel.TodoViewModel
import com.example.myapplication.ui.theme.AppIcons
import com.example.myapplication.data.models.getCategoryRankLevel
import com.example.myapplication.data.models.getCategoryRankInfo
import com.example.myapplication.data.models.calculateProgressToNextRank

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompletedTasksScreen(
    viewModel: TodoViewModel,
    onNavigateBack: () -> Unit
) {
    val tasks by viewModel.tasks.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    
    // Get all completed tasks
    val completedTasks = tasks.filter { it.isCompleted }
    
    // Group by category
    val tasksByCategory = completedTasks.groupBy { it.category }
    
    // Track expanded state for each category
    val expandedCategories = remember {
        mutableStateMapOf<TaskCategory, Boolean>().apply {
            TaskCategory.values().forEach { category ->
                this[category] = false // Initially collapsed
            }
        }
    }
    
    // Handle system back button
    BackHandler {
        onNavigateBack()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Completed Tasks") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.deleteAllCompletedTasks() }
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete all completed tasks"
                        )
                    }
                }
            )
        }
    ) { padding ->
        if (completedTasks.isEmpty()) {
            // Show empty state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No completed tasks yet",
                    style = MaterialTheme.typography.titleLarge
                )
            }
        } else {
            // Show completed tasks by category
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // For each category with completed tasks
                TaskCategory.values().forEach { category ->
                    val categoryTasks = tasksByCategory[category] ?: emptyList()
                    
                    if (categoryTasks.isNotEmpty()) {
                        item {
                            CategoryHeader(
                                category = category,
                                taskCount = categoryTasks.size,
                                categoryXp = userProfile.categoryXp[category] ?: 0,
                                userProfile = userProfile,
                                isExpanded = expandedCategories[category] ?: false,
                                onToggleExpand = { expandedCategories[category] = !(expandedCategories[category] ?: false) }
                            )
                        }
                        
                        // Only show tasks if category is expanded
                        if (expandedCategories[category] == true) {
                            items(categoryTasks.filter { !it.isSubtask() }) { task ->
                                // Get subtasks for this parent task
                                val taskSubtasks = tasks.filter { 
                                    it.parentTaskId == task.id && it.isCompleted 
                                }
                                
                                TaskItem(
                                    task = task,
                                    onComplete = { /* Already completed */ },
                                    onDelete = { viewModel.deleteTask(task) },
                                    modifier = Modifier.alpha(0.7f),
                                    subtasks = taskSubtasks,
                                    onCompleteSubtask = { /* Already completed */ }
                                )
                            }
                        }
                        
                        item {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryHeader(
    category: TaskCategory,
    taskCount: Int,
    categoryXp: Int,
    userProfile: UserProfile,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit
) {
    val categoryLevel = userProfile.categoryLevels[category] ?: 1
    
    // Get tasks completed for this category
    val tasksCompleted = userProfile.categoryTasksCompleted[category] ?: 0
    
    // Get rank information based on task count
    val rankLevel = getCategoryRankLevel(tasksCompleted)
    val rankInfo = getCategoryRankInfo(category, tasksCompleted)
    
    // Calculate progress to next rank (not level)
    val rankProgress = calculateProgressToNextRank(tasksCompleted)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = AppIcons.getCategoryIcon(category),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Text(
                    text = "Level $categoryLevel",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = AppIcons.getRankIcon(rankLevel),
                        contentDescription = "Rank icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = rankInfo.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                // XP count displayed in larger text
                Text(
                    text = "$categoryXp",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$taskCount completed tasks",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // Show/Hide tasks button
                OutlinedButton(
                    onClick = onToggleExpand,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(text = if (isExpanded) "Hide Tasks" else "Show Tasks")
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                        contentDescription = null,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
            
            LinearProgressIndicator(
                progress = { rankProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
            )
        }
    }
} 