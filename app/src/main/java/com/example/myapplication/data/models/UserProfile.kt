package com.example.myapplication.data.models

data class UserProfile(
    val totalXp: Int = 0,
    val level: Int = 1,
    val categoryLevels: Map<TaskCategory, Int> = TaskCategory.values().associateWith { 1 },
    val categoryXp: Map<TaskCategory, Int> = TaskCategory.values().associateWith { 0 },
    val categoryTasksCompleted: Map<TaskCategory, Int> = TaskCategory.values().associateWith { 0 },
    val selectedTheme: AppTheme = AppTheme.ZONE_EXPLORER,
    val taskStreak: Int = 0,
    val lastCompletedTaskDate: Long? = null
)

// Calculate level with exponential growth (20% more XP per level)
fun calculateLevel(xp: Int): Int {
    if (xp <= 0) return 1
    
    var level = 1
    var xpRequired = 100 // Base XP for level 1
    var totalXpRequired = 0
    
    while (xp > totalXpRequired + xpRequired) {
        totalXpRequired += xpRequired
        level++
        xpRequired = (xpRequired * 1.2).toInt() // 20% more XP for next level
    }
    
    return level
}

// Get XP required for next level
fun calculateXpForNextLevel(currentXp: Int): Int {
    val currentLevel = calculateLevel(currentXp)
    var xpRequired = 100 // Base XP
    var totalXpForCurrentLevel = 0
    
    // Calculate XP required for current level
    for (i in 1 until currentLevel) {
        totalXpForCurrentLevel += xpRequired
        xpRequired = (xpRequired * 1.2).toInt()
    }
    
    return totalXpForCurrentLevel + xpRequired
}

fun calculateProgressToNextLevel(currentXp: Int): Float {
    val currentLevel = calculateLevel(currentXp)
    
    // Get XP required for current and next level
    var xpForPreviousLevels = 0
    var xpForCurrentLevel = 100 // Base XP
    
    for (i in 1 until currentLevel) {
        xpForPreviousLevels += xpForCurrentLevel
        xpForCurrentLevel = (xpForCurrentLevel * 1.2).toInt()
    }
    
    val xpProgress = currentXp - xpForPreviousLevels
    
    return if (xpForCurrentLevel > 0) xpProgress.toFloat() / xpForCurrentLevel else 1f
}

fun getCategoryRankInfo(category: TaskCategory, tasksCompleted: Int): RankInfo {
    return CategoryRanks.getRankInfo(category, tasksCompleted)
}

fun getCategoryRankLevel(tasksCompleted: Int): CategoryRankLevel {
    return calculateCategoryRank(tasksCompleted)
}

fun calculateProgressToNextRank(tasksCompleted: Int): Float {
    val currentRank = calculateCategoryRank(tasksCompleted)
    val nextRankIndex = currentRank.ordinal + 1
    
    if (nextRankIndex >= CategoryRankLevel.values().size) {
        return 1f
    }
    
    val nextRank = CategoryRankLevel.values()[nextRankIndex]
    val tasksForCurrentRank = currentRank.requiredTasks
    val tasksForNextRank = nextRank.requiredTasks
    val tasksNeeded = tasksForNextRank - tasksForCurrentRank
    val tasksProgress = tasksCompleted - tasksForCurrentRank
    
    return if (tasksNeeded > 0) tasksProgress.toFloat() / tasksNeeded else 1f
}

// For debugging - this shows XP requirements for different levels
fun getXpRequirementsForLevels(): Map<Int, Int> {
    val result = mutableMapOf<Int, Int>()
    var xpRequired = 100 // Base XP for level 1
    var totalXpRequired = 0
    
    result[1] = 0 // Level 1 starts at 0 XP
    
    for (level in 2..10) {
        totalXpRequired += xpRequired
        result[level] = totalXpRequired
        xpRequired = (xpRequired * 1.2).toInt() // 20% more XP for next level
    }
    
    return result
}