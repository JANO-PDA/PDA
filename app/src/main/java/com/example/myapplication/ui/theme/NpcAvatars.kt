package com.example.myapplication.ui.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.outlined.Work
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.myapplication.data.models.Npc

/**
 * Class for managing NPC avatars
 */
object NpcAvatars {
    // Map of NPC IDs to their avatar icons
    private val npcAvatars = mapOf(
        // WORK
        "commander_varek" to Icons.Outlined.Work,
        "karlo_butcher" to Icons.Default.Construction,
        
        // STUDY
        "dr_rada" to Icons.Default.School,
        "elias_archivist" to Icons.AutoMirrored.Filled.MenuBook,
        
        // HEALTH
        "medic_tasha" to Icons.Default.HealthAndSafety,
        "brother_caleb" to Icons.Default.Healing,
        
        // PERSONAL
        "nomad" to Icons.Default.Person,
        "marika" to Icons.Default.Palette,
        
        // SHOPPING
        "grifter" to Icons.Default.ShoppingCart,
        "viktor_mule" to Icons.Default.LocalShipping,
        
        // OTHER
        "the_voice" to Icons.Default.RadioButtonChecked,
        "old_man_kaspar" to Icons.Default.Psychology
    )
    
    /**
     * Get the avatar for the specified NPC ID
     */
    fun getAvatarForNpc(npcId: String): ImageVector {
        return npcAvatars[npcId] ?: Icons.Default.Person
    }
    
    /**
     * Get the avatar for the specified NPC
     */
    fun getAvatarForNpc(npc: Npc): ImageVector {
        return getAvatarForNpc(npc.id)
    }
} 