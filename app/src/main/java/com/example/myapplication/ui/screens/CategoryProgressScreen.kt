package com.example.myapplication.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.activity.compose.BackHandler
import com.example.myapplication.data.models.*
import com.example.myapplication.ui.viewmodel.TodoViewModel
import com.example.myapplication.ui.theme.AppIcons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryProgressScreen(
    viewModel: TodoViewModel,
    onNavigateBack: () -> Unit
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val categoryStats by viewModel.categoryStats.collectAsState()

    // Handle system back button
    BackHandler {
        onNavigateBack()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Category Progress") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Your Progress by Category",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            
            items(TaskCategory.values().toList()) { category ->
                val categoryXp = userProfile.categoryXp[category] ?: 0
                val categoryLevel = userProfile.categoryLevels[category] ?: 1
                val tasksCompleted = userProfile.categoryTasksCompleted[category] ?: 0
                val rankInfo = getCategoryRankInfo(category, tasksCompleted)
                val stat = categoryStats[category]
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Category Header
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
                                    modifier = Modifier.size(32.dp)
                                )
                                Text(
                                    text = category.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Rank Information
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = AppIcons.getRankIcon(getCategoryRankLevel(tasksCompleted)),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Column {
                                Text(
                                    text = rankInfo.displayName,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = rankInfo.description,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Progress Section
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Level $categoryLevel",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "$categoryXp XP",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // XP Progress Bar
                        LinearProgressIndicator(
                            progress = { calculateProgressToNextLevel(categoryXp) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Tasks Progress
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Tasks Completed: $tasksCompleted",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            
                            val taskStats = categoryStats[category]
                            if (taskStats != null) {
                                Text(
                                    text = "Total Tasks: ${taskStats.totalTasks}",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Task Progress Bar
                        val progressPercentage = if (getCategoryRankLevel(tasksCompleted) != CategoryRankLevel.values().last()) {
                            val currentRank = getCategoryRankLevel(tasksCompleted)
                            val nextRank = CategoryRankLevel.values()[currentRank.ordinal + 1]
                            val tasksForCurrentRank = currentRank.requiredTasks
                            val tasksForNextRank = nextRank.requiredTasks
                            val tasksNeeded = tasksForNextRank - tasksForCurrentRank
                            val tasksProgress = tasksCompleted - tasksForCurrentRank
                            
                            tasksProgress.toFloat() / tasksNeeded
                        } else {
                            1f
                        }
                        
                        LinearProgressIndicator(
                            progress = { progressPercentage },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp),
                            color = MaterialTheme.colorScheme.tertiary
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Tasks to next rank
                        if (getCategoryRankLevel(tasksCompleted) != CategoryRankLevel.values().last()) {
                            val nextRank = CategoryRankLevel.values()[getCategoryRankLevel(tasksCompleted).ordinal + 1]
                            val tasksNeeded = nextRank.requiredTasks - tasksCompleted
                            
                            Text(
                                text = "$tasksNeeded more tasks to next rank",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        } else {
                            Text(
                                text = "Maximum rank achieved!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                }
            }
        }
    }
} 