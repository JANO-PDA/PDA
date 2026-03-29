package com.example.myapplication.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.myapplication.data.models.NpcMessage
import com.example.myapplication.ui.components.AnimatedBackground
import com.example.myapplication.ui.components.NpcMessageItem
import com.example.myapplication.ui.theme.GlassDialog
import com.example.myapplication.ui.theme.NpcAvatars
import com.example.myapplication.ui.viewmodel.TodoViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen(
    viewModel: TodoViewModel,
    onNavigateBack: () -> Unit
) {
    val messages by viewModel.npcMessages.collectAsState()

    var selectedMessage    by remember { mutableStateOf<NpcMessage?>(null) }
    val snackbarHostState  = remember { SnackbarHostState() }
    val scope              = rememberCoroutineScope()

    BackHandler { onNavigateBack() }

    LaunchedEffect(Unit) { viewModel.markAllMessagesAsRead() }

    AnimatedBackground {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0f),
            topBar = {
                TopAppBar(
                    title  = { Text("Contacts") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
                    ),
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                viewModel.markAllMessagesAsRead()
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message  = "All messages marked as read",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }
                        ) {
                            Icon(Icons.Default.DoneAll, "Mark all read")
                        }
                    }
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { padding ->
            if (messages.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector        = Icons.Default.Forum,
                            contentDescription = null,
                            tint               = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                            modifier           = Modifier.size(64.dp)
                        )
                        Text(
                            "No Messages Yet",
                            style     = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            "Complete tasks to receive messages from NPCs.",
                            style     = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color     = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier       = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(
                        messages.sortedByDescending { it.timestamp },
                        key = { it.id }
                    ) { message ->
                        AnimatedVisibility(
                            visible = true,
                            enter   = fadeIn(tween(200)) + slideInVertically(tween(200)) { it / 4 }
                        ) {
                            NpcMessageItem(
                                message       = message,
                                onReadMessage = { viewModel.markMessageAsRead(it) },
                                onClick       = {
                                    selectedMessage = message
                                    viewModel.markMessageAsRead(message.id)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    selectedMessage?.let { msg ->
        MessageDetailDialog(message = msg, onDismiss = { selectedMessage = null })
    }
}

@Composable
private fun MessageDetailDialog(
    message: NpcMessage,
    onDismiss: () -> Unit
) {
    val avatar        = NpcAvatars.getAvatarForNpc(message.npcId)
    val dateFormatter = SimpleDateFormat("MMM dd, yyyy · HH:mm", Locale.getDefault())

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier  = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape     = GlassDialog,
            colors    = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier          = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector        = avatar,
                            contentDescription = null,
                            tint               = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier           = Modifier.size(28.dp)
                        )
                    }
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text(
                            text       = message.npcName,
                            style      = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text  = message.category.name.lowercase().replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(Modifier.height(14.dp))

                // Message body
                Text(
                    text  = message.message,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))

                // Status + timestamp row
                Row(
                    modifier          = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector        = if (message.isFailure) Icons.Default.Cancel else Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint               = if (message.isFailure) MaterialTheme.colorScheme.error
                                             else MaterialTheme.colorScheme.primary,
                        modifier           = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text  = if (message.isFailure) "Task Failed" else "Task Completed",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (message.isFailure) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text  = dateFormatter.format(message.timestamp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End),
                    shape    = RoundedCornerShape(50)
                ) {
                    Text("Close")
                }
            }
        }
    }
}
