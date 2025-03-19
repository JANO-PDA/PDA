package com.example.myapplication.data.repository

import com.example.myapplication.data.models.Npc
import com.example.myapplication.data.models.NpcMessage
import com.example.myapplication.data.models.TaskCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Date
import kotlin.random.Random
import android.util.Log

/**
 * Repository for managing NPCs and their messages
 */
class NpcRepository {
    // List of all NPCs
    private val _npcs = MutableStateFlow<List<Npc>>(createNpcs())
    val npcs: StateFlow<List<Npc>> = _npcs.asStateFlow()
    
    // List of all messages
    private val _messages = MutableStateFlow<List<NpcMessage>>(emptyList())
    val messages: StateFlow<List<NpcMessage>> = _messages.asStateFlow()
    
    // Generate a success message for a task in the given category
    fun generateCompletionMessage(category: TaskCategory) {
        val categoryNpcs = _npcs.value.filter { it.category == category }
        if (categoryNpcs.isEmpty()) return
        
        // Randomly select an NPC from this category
        val npc = categoryNpcs.random()
        
        // Randomly select a message from the NPC's completion messages
        val message = npc.completionMessages.random()
        
        // Create and add the message
        val npcMessage = NpcMessage(
            npcId = npc.id,
            npcName = npc.name,
            npcAvatar = npc.avatar,
            message = message,
            category = category,
            timestamp = Date(),
            isFailure = false
        )
        
        _messages.value = _messages.value + npcMessage
    }
    
    // Generate a failure message for a task in the given category
    fun generateFailureMessage(category: TaskCategory) {
        val categoryNpcs = _npcs.value.filter { it.category == category }
        if (categoryNpcs.isEmpty()) return
        
        // Randomly select an NPC from this category
        val npc = categoryNpcs.random()
        
        // Randomly select a message from the NPC's failure messages
        val message = npc.failureMessages.random()
        
        // Create and add the message
        val npcMessage = NpcMessage(
            npcId = npc.id,
            npcName = npc.name,
            npcAvatar = npc.avatar,
            message = message,
            category = category,
            timestamp = Date(),
            isFailure = true
        )
        
        _messages.value = _messages.value + npcMessage
        
        // Log for debugging
        Log.d("NpcRepository", "Generated failure message: ${npcMessage.message} for category: ${category.name}")
    }
    
    // Mark a message as read
    fun markMessageAsRead(messageId: String) {
        _messages.value = _messages.value.map { 
            if (it.id == messageId) it.copy(isRead = true) else it 
        }
    }
    
    // Mark all messages as read
    fun markAllMessagesAsRead() {
        _messages.value = _messages.value.map { it.copy(isRead = true) }
    }
    
    // Get unread message count
    fun getUnreadMessageCount(): Int {
        return _messages.value.count { !it.isRead }
    }
    
    // Create all NPCs based on the provided data
    private fun createNpcs(): List<Npc> {
        return listOf(
            // WORK NPCs
            Npc(
                id = "commander_varek",
                name = "Commander Varek",
                avatar = "npc_varek",
                category = TaskCategory.WORK,
                personality = "Strict, pragmatic, and relentless.",
                completionMessages = listOf(
                    "You did what needed to be done. That's how survivors are made.",
                    "Keep this up, and maybe you'll earn something more than just survival.",
                    "Good. There's no room for slackers here."
                ),
                failureMessages = listOf(
                    "One task undone is one crack in the foundation. Enough cracks, and everything collapses.",
                    "You think you'll get another chance? The wasteland doesn't do second chances.",
                    "Discipline wins wars. Laziness loses them."
                ),
                isPrimary = true
            ),
            Npc(
                id = "karlo_butcher",
                name = "Karlo \"The Butcher\"",
                avatar = "npc_karlo",
                category = TaskCategory.WORK,
                personality = "Gruff, no-nonsense, dark sense of humor.",
                completionMessages = listOf(
                    "Work ain't pretty, but it keeps your hands from shaking.",
                    "You get your job done, you get to eat. Simple as that.",
                    "Not bad. You might actually survive out here."
                ),
                failureMessages = listOf(
                    "I used to know a guy who skipped work too much. He's fertilizer now.",
                    "You think someone else is gonna carry your weight? Not in this world.",
                    "Idle hands bring bad luck. You don't want bad luck."
                )
            ),
            
            // STUDY NPCs
            Npc(
                id = "dr_rada",
                name = "Dr. Rada",
                avatar = "npc_rada",
                category = TaskCategory.STUDY,
                personality = "Intellectual, slightly eccentric, driven by curiosity.",
                completionMessages = listOf(
                    "You've added another piece to the puzzle. Don't stop now.",
                    "A sharp mind is more dangerous than a dull blade.",
                    "If knowledge dies, so does the world. Keep learning."
                ),
                failureMessages = listOf(
                    "The ignorant don't last long out here.",
                    "Every day you waste, history forgets another name. Don't let it be yours.",
                    "You didn't study? Then you just chose to be prey."
                ),
                isPrimary = true
            ),
            Npc(
                id = "elias_archivist",
                name = "Elias \"The Archivist\"",
                avatar = "npc_elias",
                category = TaskCategory.STUDY,
                personality = "Nostalgic, melancholic, but determined.",
                completionMessages = listOf(
                    "You remind me of the students I once taught. Before the silence took everything.",
                    "You may not see it now, but this knowledge will save you one day.",
                    "One more page remembered, one less piece of the past lost."
                ),
                failureMessages = listOf(
                    "A world without knowledge is a world without light. Do you really want to live in the dark?",
                    "I've seen bright minds waste away because they stopped caring. Don't follow their path.",
                    "Ignoring wisdom is like ignoring a loaded gun pointed at you."
                )
            ),
            
            // HEALTH NPCs
            Npc(
                id = "medic_tasha",
                name = "Medic Tasha",
                avatar = "npc_tasha",
                category = TaskCategory.HEALTH,
                personality = "Compassionate but no-nonsense.",
                completionMessages = listOf(
                    "Taking care of yourself isn't selfish. It's necessary.",
                    "A strong body keeps you alive. A weak one makes you a target.",
                    "You're one of the few who actually listen. Good."
                ),
                failureMessages = listOf(
                    "Neglect yourself, and the world will do the rest.",
                    "I don't patch up fools. Take care of yourself.",
                    "Sick, weak, tired—pick any of those and you won't last long."
                ),
                isPrimary = true
            ),
            Npc(
                id = "brother_caleb",
                name = "Brother Caleb",
                avatar = "npc_caleb",
                category = TaskCategory.HEALTH,
                personality = "Calm, philosophical, slightly eerie.",
                completionMessages = listOf(
                    "You heal yourself, you heal the world in small ways.",
                    "Good. Life is a fragile ember—you must tend it carefully.",
                    "Pain is a lesson. Strength is its reward."
                ),
                failureMessages = listOf(
                    "The body is a temple. Yours is crumbling.",
                    "Even the strongest fall when their health fades.",
                    "Do you wish to meet the void so soon?"
                )
            ),
            
            // PERSONAL NPCs
            Npc(
                id = "nomad",
                name = "Nomad",
                avatar = "npc_nomad",
                category = TaskCategory.PERSONAL,
                personality = "Mysterious, poetic, deeply introspective.",
                completionMessages = listOf(
                    "You did something for yourself. That's rare in a world that only takes.",
                    "Even the smallest step forward is still movement.",
                    "You are not just surviving. You are living. Keep going."
                ),
                failureMessages = listOf(
                    "A road unwalked is a journey lost.",
                    "You ignore yourself today, you lose yourself tomorrow.",
                    "Your soul needs care too. Don't let it wither."
                ),
                isPrimary = true
            ),
            Npc(
                id = "marika",
                name = "Marika",
                avatar = "npc_marika",
                category = TaskCategory.PERSONAL,
                personality = "Wistful, slightly melancholic but hopeful.",
                completionMessages = listOf(
                    "You found time for yourself. That means you're still human.",
                    "Personal time is like a rare artifact—cherish it.",
                    "A little beauty in this world of ash? That's worth something."
                ),
                failureMessages = listOf(
                    "Too busy for yourself? That's how people become empty shells.",
                    "Neglecting what makes you you? Dangerous mistake.",
                    "You can run from yourself, but you won't get far."
                )
            ),
            
            // SHOPPING NPCs
            Npc(
                id = "grifter",
                name = "Grifter",
                avatar = "npc_grifter",
                category = TaskCategory.SHOPPING,
                personality = "Cunning, sarcastic, always looking for a deal.",
                completionMessages = listOf(
                    "You got what you needed. Smart move.",
                    "Preparedness is the difference between a survivor and a corpse.",
                    "You planned ahead? Good. That's rare."
                ),
                failureMessages = listOf(
                    "No supplies? No problem. Just hope you don't get hungry.",
                    "A fool and their last meal are soon parted.",
                    "Ever seen someone desperate? It's not pretty. Stock up next time."
                ),
                isPrimary = true
            ),
            Npc(
                id = "viktor_mule",
                name = "Viktor \"The Mule\"",
                avatar = "npc_viktor",
                category = TaskCategory.SHOPPING,
                personality = "Gruff, but secretly enjoys helping people.",
                completionMessages = listOf(
                    "You stocked up? You're smarter than most.",
                    "A full bag today keeps desperation away tomorrow.",
                    "Supplies are life. Don't forget that."
                ),
                failureMessages = listOf(
                    "No supplies? Hope you like bartering with bullets.",
                    "Hungry, cold, unprepared—you're setting yourself up for trouble.",
                    "Ran out of essentials? What's next, selling your boots?"
                )
            ),
            
            // OTHER NPCs
            Npc(
                id = "the_voice",
                name = "The Voice",
                avatar = "npc_voice",
                category = TaskCategory.OTHER,
                personality = "Mysterious radio broadcaster.",
                completionMessages = listOf(
                    "Every action changes the tide. You did well.",
                    "A step forward is a step away from the abyss.",
                    "The world watches. Keep moving."
                ),
                failureMessages = listOf(
                    "Stagnation is a slow death. Beware.",
                    "You lost today, but tomorrow is unwritten.",
                    "Even shadows move. Why don't you?"
                ),
                isPrimary = true
            ),
            Npc(
                id = "old_man_kaspar",
                name = "Old Man Kaspar",
                avatar = "npc_kaspar",
                category = TaskCategory.OTHER,
                personality = "Crazy hermit, speaks in riddles.",
                completionMessages = listOf(
                    "The gears turn, the wheel moves. Keep pushing.",
                    "Another day won, another ghost left behind."
                ),
                failureMessages = listOf(
                    "A stalled engine rusts away. Get moving.",
                    "The stars whisper warnings. Listen before it's too late."
                )
            )
        )
    }
} 