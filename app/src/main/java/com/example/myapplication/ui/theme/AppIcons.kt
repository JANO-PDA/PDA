package com.example.myapplication.ui.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.myapplication.data.models.TaskCategory
import com.example.myapplication.data.models.CategoryRankLevel
import com.example.myapplication.data.models.calculateCategoryRank

object AppIcons {
    // Category Icons
    fun getCategoryIcon(category: TaskCategory): ImageVector {
        return when (category) {
            TaskCategory.WORK -> Icons.Default.Build          // Tools icon
            TaskCategory.STUDY -> Icons.Default.Info          // Info icon
            TaskCategory.HEALTH -> Icons.Default.Favorite     // Heart icon
            TaskCategory.PERSONAL -> Icons.Default.Person     // Person icon
            TaskCategory.SHOPPING -> Icons.Default.ShoppingCart // Shopping cart icon
            TaskCategory.OTHER -> Icons.Default.Star          // Star for misc
        }
    }

    // Rank Icons - Different icons based on rank level
    fun getRankIcon(rankLevel: CategoryRankLevel): ImageVector {
        return when (rankLevel) {
            // Novice ranks
            CategoryRankLevel.LEVEL_1 -> Icons.Default.Star
            CategoryRankLevel.LEVEL_2 -> Icons.Default.Check
            
            // Intermediate ranks
            CategoryRankLevel.LEVEL_3 -> Icons.Default.CheckCircle
            CategoryRankLevel.LEVEL_4 -> Icons.Default.Done
            
            // Advanced ranks
            CategoryRankLevel.LEVEL_5 -> Icons.Default.Favorite
            CategoryRankLevel.LEVEL_6 -> Icons.Default.Settings
            
            // Expert ranks
            CategoryRankLevel.LEVEL_7 -> Icons.Default.Warning
            CategoryRankLevel.LEVEL_8 -> Icons.Default.Star
            
            // Master ranks
            CategoryRankLevel.LEVEL_9 -> Icons.Default.Edit
            CategoryRankLevel.LEVEL_10 -> Icons.Default.Star
        }
    }
    
    // Alternative method to get rank icon by XP directly
    fun getRankIconByXp(xp: Int): ImageVector {
        val rankLevel = calculateCategoryRank(xp)
        return getRankIcon(rankLevel)
    }
} 