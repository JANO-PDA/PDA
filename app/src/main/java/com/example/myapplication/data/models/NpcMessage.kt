package com.example.myapplication.data.models

import java.util.UUID
import java.util.Date

/**
 * Represents a message from an NPC
 */
data class NpcMessage(
    val id: String = UUID.randomUUID().toString(),
    val npcId: String,
    val npcName: String,
    val npcAvatar: String, // Resource ID or URL
    val message: String,
    val category: TaskCategory,
    val timestamp: Date = Date(),
    val isRead: Boolean = false,
    val isFailure: Boolean = false // Whether the message is for task failure
)

/**
 * Represents an NPC character
 */
data class Npc(
    val id: String,
    val name: String,
    val avatar: String, // Resource ID or URL
    val category: TaskCategory,
    val personality: String,
    val completionMessages: List<String>,
    val failureMessages: List<String>,
    val isPrimary: Boolean = false // Whether this is the primary NPC for the category
) 