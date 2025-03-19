package com.example.myapplication.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.models.NpcMessage
import com.example.myapplication.data.models.TaskCategory
import com.example.myapplication.ui.theme.NpcAvatars
import java.text.SimpleDateFormat
import java.util.*

/**
 * Component to display a single NPC message
 */
@Composable
fun NpcMessageItem(
    message: NpcMessage,
    modifier: Modifier = Modifier,
    onReadMessage: (String) -> Unit = {},
    onClick: () -> Unit = {}
) {
    val isRead = message.isRead
    val categoryColor = getCategoryColor(message.category)
    val avatarIcon = NpcAvatars.getAvatarForNpc(message.npcId)
    val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    val dateFormatter = SimpleDateFormat("MMM dd", Locale.getDefault())
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (!isRead) 4.dp else 1.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (!isRead) 
                categoryColor.copy(alpha = 0.1f)
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(categoryColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = avatarIcon,
                    contentDescription = null,
                    tint = categoryColor,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Message content
            Column(modifier = Modifier.weight(1f)) {
                // Header: NPC name and time
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = message.npcName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (!isRead) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                    
                    Text(
                        text = if (isWithin24Hours(message.timestamp)) 
                            timeFormatter.format(message.timestamp)
                        else
                            dateFormatter.format(message.timestamp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Message
                Text(
                    text = message.message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (!isRead) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Task status indicator
                Box(
                    modifier = Modifier
                        .align(Alignment.End)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (message.isFailure) 
                                MaterialTheme.colorScheme.error.copy(alpha = 0.1f) 
                            else 
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (message.isFailure) "Failed Task" else "Completed Task",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (message.isFailure) 
                            MaterialTheme.colorScheme.error 
                        else 
                            MaterialTheme.colorScheme.primary
                    )
                }
                
                // Mark as read indicator
                if (!isRead) {
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = { onReadMessage(message.id) },
                        modifier = Modifier.align(Alignment.End),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Text("Mark as read")
                    }
                }
            }
        }
    }
}

/**
 * Check if the timestamp is within the last 24 hours
 */
private fun isWithin24Hours(timestamp: Date): Boolean {
    val now = System.currentTimeMillis()
    val diff = now - timestamp.time
    return diff < 24 * 60 * 60 * 1000
}

/**
 * Get color for task category
 */
@Composable
private fun getCategoryColor(category: TaskCategory): Color {
    return when (category) {
        TaskCategory.WORK -> MaterialTheme.colorScheme.tertiary
        TaskCategory.STUDY -> MaterialTheme.colorScheme.primary
        TaskCategory.HEALTH -> MaterialTheme.colorScheme.secondary
        TaskCategory.PERSONAL -> MaterialTheme.colorScheme.error
        TaskCategory.SHOPPING -> MaterialTheme.colorScheme.primaryContainer
        TaskCategory.OTHER -> MaterialTheme.colorScheme.inversePrimary
    }
} 