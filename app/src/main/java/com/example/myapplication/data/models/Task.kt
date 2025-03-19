package com.example.myapplication.data.models

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.UUID

data class Task(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String = "",
    val difficulty: TaskDifficulty,
    val category: TaskCategory,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val dueDate: LocalDate? = null,
    val dueTime: LocalTime? = null,
    val parentTaskId: String? = null,
    val subtasks: List<String> = emptyList(),
    val hasReminder: Boolean = false
) {
    fun getXpReward(): Int {
        return when (difficulty) {
            TaskDifficulty.EASY -> 10
            TaskDifficulty.MEDIUM -> 25
            TaskDifficulty.HARD -> 50
            TaskDifficulty.NIGHTMARE -> 100
        }
    }
    
    fun getDueDateTime(): LocalDateTime? {
        return if (dueDate != null) {
            LocalDateTime.of(dueDate, dueTime ?: LocalTime.of(9, 0)) // Default to 9:00 AM if no time
        } else {
            null
        }
    }
    
    fun isOverdue(): Boolean {
        val today = LocalDate.now()
        val now = LocalDateTime.now()
        
        return if (dueDate != null && dueTime != null) {
            // If we have both date and time, compare with current date and time
            !isCompleted && now.isAfter(LocalDateTime.of(dueDate, dueTime))
        } else {
            // If we only have date, just compare the dates
            dueDate != null && !isCompleted && today.isAfter(dueDate)
        }
    }
    
    fun isDueSoon(): Boolean {
        if (dueDate == null || isCompleted) return false
        
        val today = LocalDate.now()
        val now = LocalDateTime.now()
        
        if (dueTime != null) {
            // If we have both date and time
            val dueDateTime = LocalDateTime.of(dueDate, dueTime)
            val oneDayFromNow = now.plusDays(1)
            
            // Consider "due soon" if within the next 24 hours
            return now.isBefore(dueDateTime) && dueDateTime.isBefore(oneDayFromNow)
        } else {
            // If we only have date
            return dueDate == today || dueDate == today.plusDays(1)
        }
    }
    
    fun isSubtask(): Boolean {
        return parentTaskId != null
    }
    
    fun hasSubtasks(): Boolean {
        return subtasks.isNotEmpty()
    }
}