package com.example.myapplication.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.models.NpcMessage
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun NotificationTab(
    messages: List<NpcMessage>,
    onMessageClick: (NpcMessage) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(messages) { message ->
            NotificationItem(
                message = message,
                onClick = { onMessageClick(message) }
            )
        }
    }
}

@Composable
private fun NotificationItem(
    message: NpcMessage,
    onClick: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with NPC name and timestamp
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "NPC",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = message.npcName,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                
                Text(
                    text = dateFormat.format(message.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Message content
            Text(
                text = message.message,
                style = MaterialTheme.typography.bodyLarge,
                color = if (!message.isRead) 
                    MaterialTheme.colorScheme.onSurface 
                else 
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Status indicator
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (message.isFailure) 
                            MaterialTheme.colorScheme.error.copy(alpha = 0.1f) 
                        else 
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = if (message.isFailure) 
                            Icons.Default.Warning 
                        else 
                            Icons.Default.CheckCircle,
                        contentDescription = if (message.isFailure) "Failed" else "Completed",
                        modifier = Modifier.size(16.dp),
                        tint = if (message.isFailure) 
                            MaterialTheme.colorScheme.error 
                        else 
                            MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = if (message.isFailure) "Failed Task" else "Completed Task",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (message.isFailure) 
                            MaterialTheme.colorScheme.error 
                        else 
                            MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
} 