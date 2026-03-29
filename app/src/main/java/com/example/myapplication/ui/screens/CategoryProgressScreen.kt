package com.example.myapplication.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.models.*
import com.example.myapplication.ui.components.AnimatedBackground
import com.example.myapplication.ui.theme.AppIcons
import com.example.myapplication.ui.theme.GlassCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryProgressScreen(
    viewModel: com.example.myapplication.ui.viewmodel.TodoViewModel,
    onNavigateBack: () -> Unit
) {
    val userProfile by viewModel.userProfile.collectAsState()

    BackHandler { onNavigateBack() }

    AnimatedBackground {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0f),
            topBar = {
                TopAppBar(
                    title = { Text("Category Stats") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
                    ),
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                        }
                    }
                )
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(TaskCategory.entries) { category ->
                    val categoryXp       = userProfile.categoryXp[category] ?: 0
                    val categoryLevel    = userProfile.categoryLevels[category] ?: 1
                    val tasksCompleted   = userProfile.categoryTasksCompleted[category] ?: 0
                    val rankInfo         = getCategoryRankInfo(category, tasksCompleted)
                    val xpProgress       = calculateProgressToNextLevel(categoryXp)

                    CategoryStatCard(
                        category       = category,
                        categoryXp     = categoryXp,
                        categoryLevel  = categoryLevel,
                        tasksCompleted = tasksCompleted,
                        rankInfo       = rankInfo,
                        xpProgress     = xpProgress
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryStatCard(
    category: TaskCategory,
    categoryXp: Int,
    categoryLevel: Int,
    tasksCompleted: Int,
    rankInfo: RankInfo,
    xpProgress: Float
) {
    val accent = categoryAccentColor(category)

    val animXpProgress by animateFloatAsState(
        targetValue   = xpProgress,
        animationSpec = tween(700, easing = FastOutSlowInEasing),
        label         = "xpProg"
    )

    val rankProgress = rankProgressFor(tasksCompleted)
    val animRankProg by animateFloatAsState(
        targetValue   = rankProgress,
        animationSpec = tween(700, easing = FastOutSlowInEasing),
        label         = "rankProg"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = GlassCard,
        colors   = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: icon + category name + level badge
            Row(
                modifier              = Modifier.fillMaxWidth(),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(accent.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector        = AppIcons.getCategoryIcon(category),
                            contentDescription = null,
                            tint               = accent,
                            modifier           = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text       = category.name.lowercase().replaceFirstChar { it.uppercase() },
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                // Level pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(accent.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text  = "Lv $categoryLevel",
                        style = MaterialTheme.typography.labelMedium,
                        color = accent,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // Rank row
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector        = AppIcons.getRankIcon(getCategoryRankLevel(tasksCompleted)),
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.primary,
                    modifier           = Modifier.size(16.dp)
                )
                Text(
                    text  = rankInfo.displayName,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text  = "— ${rankInfo.description}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                )
            }

            Spacer(Modifier.height(12.dp))

            // XP bar
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "XP Progress",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Text(
                    "$categoryXp XP",
                    style      = MaterialTheme.typography.labelSmall,
                    color      = accent,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(Modifier.height(4.dp))
            LinearProgressIndicator(
                progress  = { animXpProgress },
                modifier  = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(50)),
                color      = accent,
                trackColor = accent.copy(alpha = 0.15f)
            )

            Spacer(Modifier.height(10.dp))

            // Rank progress bar
            val rankLevel   = getCategoryRankLevel(tasksCompleted)
            val isMaxRank   = rankLevel == CategoryRankLevel.entries.last()
            val nextRankLabel = if (!isMaxRank) {
                val next = CategoryRankLevel.entries[rankLevel.ordinal + 1]
                val needed = next.requiredTasks - tasksCompleted
                "$needed tasks to ${next.name.lowercase().replaceFirstChar { it.uppercase() }}"
            } else "Max rank achieved"

            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Rank Progress",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Text(
                    nextRankLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
            Spacer(Modifier.height(4.dp))
            LinearProgressIndicator(
                progress  = { animRankProg },
                modifier  = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(50)),
                color      = MaterialTheme.colorScheme.tertiary,
                trackColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
            )

            Spacer(Modifier.height(6.dp))

            Text(
                "$tasksCompleted tasks completed",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
            )
        }
    }
}

private fun rankProgressFor(tasksCompleted: Int): Float {
    val rankLevel = getCategoryRankLevel(tasksCompleted)
    val isMax     = rankLevel == CategoryRankLevel.entries.last()
    if (isMax) return 1f
    val next          = CategoryRankLevel.entries[rankLevel.ordinal + 1]
    val tasksNeeded   = next.requiredTasks - rankLevel.requiredTasks
    val tasksProgress = tasksCompleted - rankLevel.requiredTasks
    return (tasksProgress.toFloat() / tasksNeeded).coerceIn(0f, 1f)
}

private fun categoryAccentColor(category: TaskCategory): androidx.compose.ui.graphics.Color = when (category) {
    TaskCategory.WORK     -> com.example.myapplication.ui.theme.CategoryWork
    TaskCategory.STUDY    -> com.example.myapplication.ui.theme.CategoryStudy
    TaskCategory.HEALTH   -> com.example.myapplication.ui.theme.CategoryHealth
    TaskCategory.PERSONAL -> com.example.myapplication.ui.theme.CategoryPersonal
    TaskCategory.SHOPPING -> com.example.myapplication.ui.theme.CategoryShopping
    TaskCategory.OTHER    -> com.example.myapplication.ui.theme.CategoryOther
}
