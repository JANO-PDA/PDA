package com.example.myapplication.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.models.NpcMessage
import com.example.myapplication.data.models.TaskCategory
import com.example.myapplication.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Chat-bubble style NPC message card.
 * Unread messages get a coloured accent border and brighter bubble.
 */
@Composable
fun NpcMessageItem(
    message: NpcMessage,
    modifier: Modifier = Modifier,
    onReadMessage: (String) -> Unit = {},
    onClick: () -> Unit = {}
) {
    val accent    = categoryAccentColor(message.category)
    val avatarIcon = NpcAvatars.getAvatarForNpc(message.npcId)
    val timeStr   = formatMessageTime(message.timestamp)
    val isUnread  = !message.isRead

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.Top
    ) {
        // ─ Avatar circle ──────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(accent.copy(alpha = if (isUnread) 0.25f else 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = avatarIcon,
                contentDescription = message.npcName,
                tint = accent,
                modifier = Modifier.size(26.dp)
            )
        }

        Spacer(Modifier.width(12.dp))

        // ─ Bubble ─────────────────────────────────────────────────────────────
        Column(modifier = Modifier.weight(1f)) {
            // Header row: name + time + unread dot
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text  = message.npcName,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (isUnread) FontWeight.Bold else FontWeight.SemiBold,
                        color = if (isUnread) MaterialTheme.colorScheme.onSurface
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                    )
                    if (isUnread) {
                        Spacer(Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(accent)
                        )
                    }
                }
                Text(
                    text  = timeStr,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                )
            }

            Spacer(Modifier.height(4.dp))

            // Bubble background
            val bubbleBg = when {
                message.isFailure -> MaterialTheme.colorScheme.error.copy(alpha = if (isUnread) 0.14f else 0.07f)
                isUnread          -> accent.copy(alpha = 0.13f)
                else              -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(
                        androidx.compose.foundation.shape.RoundedCornerShape(
                            topStart = 2.dp, topEnd = 12.dp, bottomEnd = 12.dp, bottomStart = 12.dp
                        )
                    )
                    .background(bubbleBg)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Column {
                    Text(
                        text     = message.message,
                        style    = MaterialTheme.typography.bodyMedium,
                        color    = if (isUnread) MaterialTheme.colorScheme.onSurface
                                   else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(Modifier.height(6.dp))

                    // Status pill
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (message.isFailure) Icons.Default.Cancel else Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (message.isFailure) MaterialTheme.colorScheme.error
                                   else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text  = if (message.isFailure) "Task failed" else "Task completed",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (message.isFailure) MaterialTheme.colorScheme.error
                                    else MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

// ─── Helpers ──────────────────────────────────────────────────────────────────

private fun categoryAccentColor(category: TaskCategory): Color = when (category) {
    TaskCategory.WORK     -> CategoryWork
    TaskCategory.STUDY    -> CategoryStudy
    TaskCategory.HEALTH   -> CategoryHealth
    TaskCategory.PERSONAL -> CategoryPersonal
    TaskCategory.SHOPPING -> CategoryShopping
    TaskCategory.OTHER    -> CategoryOther
}

private fun formatMessageTime(timestamp: Date): String {
    val diff = System.currentTimeMillis() - timestamp.time
    return when {
        diff < 60_000L         -> "just now"
        diff < 3_600_000L      -> "${diff / 60_000}m ago"
        diff < 86_400_000L     -> SimpleDateFormat("HH:mm", Locale.getDefault()).format(timestamp)
        else                   -> SimpleDateFormat("MMM dd", Locale.getDefault()).format(timestamp)
    }
}
