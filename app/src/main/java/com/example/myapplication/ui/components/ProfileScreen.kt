package com.example.myapplication.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.models.AppTheme
import com.example.myapplication.data.models.TaskCategory
import com.example.myapplication.data.models.UserProfile
import com.example.myapplication.ui.viewmodel.CategoryStats

@Composable
fun ProfileScreen(
    userProfile: UserProfile,
    categoryStats: Map<TaskCategory, CategoryStats>,
    onThemeChange: (AppTheme) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // User Profile Card
        item {
            UserProfileCard(
                userProfile = userProfile,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }
        
        // Theme Selection
        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Theme",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AppTheme.values().forEach { theme ->
                            FilterChip(
                                selected = userProfile.selectedTheme == theme,
                                onClick = { onThemeChange(theme) },
                                label = { Text(theme.name) }
                            )
                        }
                    }
                }
            }
        }
        
        // Category Stats
        item {
            Text(
                text = "Category Progress",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        
        // Category stat items
        items(categoryStats.toList()) { (category, stats) ->
            CategoryProgressCard(
                category = category,
                stats = stats,
                userProfile = userProfile
            )
        }
    }
}

@Composable
private fun CategoryProgressCard(
    category: TaskCategory,
    stats: CategoryStats,
    userProfile: UserProfile,
    modifier: Modifier = Modifier
) {
    val categoryLevel = userProfile.categoryLevels[category] ?: 1
    val xp = userProfile.categoryXp[category] ?: 0
    // Calculate progress as a percentage of completion rate
    val progress = stats.completionRate
    
    Card(
        modifier = modifier.fillMaxWidth()
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
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "Level $categoryLevel",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Progress bar
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${stats.completedTasks} completed",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "$xp XP",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
} 