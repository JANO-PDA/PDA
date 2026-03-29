package com.example.myapplication.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.models.*
import com.example.myapplication.ui.components.AnimatedBackground
import com.example.myapplication.ui.components.EmptyStateLottie
import com.example.myapplication.ui.components.TaskItem
import com.example.myapplication.ui.theme.AppIcons
import com.example.myapplication.ui.theme.GlassCard
import com.example.myapplication.ui.viewmodel.TodoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompletedTasksScreen(
    viewModel: TodoViewModel,
    onNavigateBack: () -> Unit
) {
    val tasks        by viewModel.tasks.collectAsState()
    val userProfile  by viewModel.userProfile.collectAsState()

    val completedTasks   = tasks.filter { it.isCompleted }
    val tasksByCategory  = completedTasks.groupBy { it.category }
    val expandedCategories = remember {
        mutableStateMapOf<TaskCategory, Boolean>().apply {
            TaskCategory.entries.forEach { this[it] = false }
        }
    }

    BackHandler { onNavigateBack() }

    AnimatedBackground {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0f),
            topBar = {
                TopAppBar(
                    title  = { Text("Completed Tasks") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
                    ),
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.deleteAllCompletedTasks() }) {
                            Icon(Icons.Default.Delete, "Delete all completed")
                        }
                    }
                )
            }
        ) { padding ->
            if (completedTasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        EmptyStateLottie(size = 140.dp)
                        Text(
                            "No completed tasks yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    TaskCategory.entries.forEach { category ->
                        val categoryTasks = tasksByCategory[category] ?: emptyList()
                        if (categoryTasks.isEmpty()) return@forEach

                        item(key = "header_${category.name}") {
                            CompletedCategoryHeader(
                                category       = category,
                                taskCount      = categoryTasks.size,
                                categoryXp     = userProfile.categoryXp[category] ?: 0,
                                userProfile    = userProfile,
                                isExpanded     = expandedCategories[category] ?: false,
                                onToggleExpand = {
                                    expandedCategories[category] = !(expandedCategories[category] ?: false)
                                }
                            )
                        }

                        if (expandedCategories[category] == true) {
                            items(
                                categoryTasks.filter { !it.isSubtask() },
                                key = { it.id }
                            ) { task ->
                                val taskSubtasks = tasks.filter {
                                    it.parentTaskId == task.id && it.isCompleted
                                }
                                TaskItem(
                                    task     = task,
                                    onComplete = {},
                                    onDelete = { viewModel.deleteTask(task) },
                                    modifier = Modifier.alpha(0.7f),
                                    subtasks = taskSubtasks
                                )
                            }
                        }

                        item(key = "divider_${category.name}") {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CompletedCategoryHeader(
    category: TaskCategory,
    taskCount: Int,
    categoryXp: Int,
    userProfile: UserProfile,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit
) {
    val categoryLevel  = userProfile.categoryLevels[category] ?: 1
    val tasksCompleted = userProfile.categoryTasksCompleted[category] ?: 0
    val rankLevel      = getCategoryRankLevel(tasksCompleted)
    val rankInfo       = getCategoryRankInfo(category, tasksCompleted)

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = GlassCard,
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector        = AppIcons.getCategoryIcon(category),
                        contentDescription = null,
                        tint               = MaterialTheme.colorScheme.primary,
                        modifier           = Modifier.size(24.dp)
                    )
                    Text(
                        text       = category.name.lowercase().replaceFirstChar { it.uppercase() },
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text       = "Lv $categoryLevel",
                    style      = MaterialTheme.typography.labelMedium,
                    color      = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(6.dp))

            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector        = AppIcons.getRankIcon(rankLevel),
                        contentDescription = null,
                        tint               = MaterialTheme.colorScheme.primary,
                        modifier           = Modifier.size(16.dp)
                    )
                    Text(
                        text  = rankInfo.displayName,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text       = "$categoryXp XP",
                    style      = MaterialTheme.typography.labelMedium,
                    color      = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    text  = "$taskCount completed",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                OutlinedButton(
                    onClick          = onToggleExpand,
                    contentPadding   = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    shape            = RoundedCornerShape(50)
                ) {
                    Text(
                        text  = if (isExpanded) "Hide" else "Show",
                        style = MaterialTheme.typography.labelSmall
                    )
                    Icon(
                        imageVector        = if (isExpanded) Icons.Default.KeyboardArrowUp
                                             else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        modifier           = Modifier
                            .padding(start = 4.dp)
                            .size(16.dp)
                    )
                }
            }
        }
    }
}
