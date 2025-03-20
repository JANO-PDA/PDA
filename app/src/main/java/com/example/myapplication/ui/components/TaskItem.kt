package com.example.myapplication.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.models.Task
import com.example.myapplication.data.models.TaskDifficulty
import com.example.myapplication.data.models.TaskCategory
import com.example.myapplication.ui.theme.AppIcons
import com.example.myapplication.ui.theme.lineThrough
import com.example.myapplication.ui.theme.none
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.delay

@Composable
fun TaskItem(
    task: Task,
    onComplete: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    subtasks: List<Task> = emptyList(),
    onAddSubtask: ((Task) -> Unit)? = null,
    onCompleteSubtask: ((Task) -> Unit)? = null,
    level: Int = 0, // Indentation level for subtasks
    isHighlighted: Boolean = false
) {
    var isCompleting by remember { mutableStateOf(false) }
    var isExpanded by remember { mutableStateOf(isHighlighted) } // Auto-expand if highlighted
    val scope = rememberCoroutineScope()
    
    // Remember that we were highlighted (for animation purposes)
    var wasHighlighted by remember { mutableStateOf(false) }
    
    // Update wasHighlighted if isHighlighted changes
    LaunchedEffect(isHighlighted) {
        if (isHighlighted) {
            wasHighlighted = true
            // Auto-expand highlighted tasks
            isExpanded = true
        }
    }
    
    // Animation values with enhanced effects
    val scale by animateFloatAsState(
        targetValue = if (isCompleting) 0.8f else if (isHighlighted) 1.05f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )
    
    val alpha by animateFloatAsState(
        targetValue = if (isCompleting) 0f else 1f,
        animationSpec = tween(durationMillis = 200),
        label = "alpha"
    )
    
    // Highlight pulse animation
    val highlightAnim = rememberInfiniteTransition(label = "highlightPulse")
    val highlightPulse by highlightAnim.animateFloat(
        initialValue = 1f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "highlightPulse"
    )
    
    // Determine card background color based on task status
    val isOverdue = task.isOverdue()
    val isDueSoon = task.isDueSoon()
    
    // Enhanced background color transition
    val backgroundColor by animateColorAsState(
        targetValue = when {
            isHighlighted -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
            isCompleting -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            isOverdue -> MaterialTheme.colorScheme.errorContainer
            isDueSoon -> MaterialTheme.colorScheme.tertiaryContainer
            else -> MaterialTheme.colorScheme.surface
        },
        animationSpec = tween(
            durationMillis = 150,
            easing = LinearOutSlowInEasing
        ),
        label = "background"
    )
    
    // Border width animation for highlighted items
    val borderWidth by animateFloatAsState(
        targetValue = if (wasHighlighted) {
            if (isHighlighted) 2f else 0f
        } else 0f,
        animationSpec = tween(durationMillis = 500),
        label = "borderWidth"
    )
    
    // Rotation for dropdown arrow with smoother effect
    val arrowRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(
            durationMillis = 200,
            easing = FastOutSlowInEasing
        ),
        label = "arrowRotation"
    )
    
    // Apply scaling based on highlight state
    val itemScale = if (isHighlighted) highlightPulse else scale
    
    // Animate content height changes
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = (level * 16).dp // Indentation for subtasks
            )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .scale(itemScale)
                .alpha(alpha)
                .animateContentSize(
                    animationSpec = tween(
                        durationMillis = 150, 
                        easing = LinearOutSlowInEasing
                    )
                ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (isHighlighted) 8.dp else 2.dp
            ),
            colors = CardDefaults.cardColors(containerColor = backgroundColor),
            border = if (borderWidth > 0) {
                BorderStroke(
                    width = borderWidth.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            } else null
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Category icon with pulse animation
                            val pulseAnim = rememberInfiniteTransition(label = "iconPulse")
                            val iconScale by pulseAnim.animateFloat(
                                initialValue = 0.9f,
                                targetValue = 1.1f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(1500),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "iconPulse"
                            )
                            
                            Icon(
                                imageVector = AppIcons.getCategoryIcon(task.category),
                                contentDescription = "${task.category.name} icon",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .size(24.dp)
                                    .scale(if (isDueSoon || isOverdue) iconScale else 1f)
                            )
                            
                            Text(
                                text = task.title,
                                style = MaterialTheme.typography.titleMedium,
                                textDecoration = if (task.isCompleted) lineThrough else none,
                                color = if (task.isCompleted) 
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                else 
                                    MaterialTheme.colorScheme.onSurface
                            )
                            
                            // Status indicators with animation
                            if (isOverdue) {
                                val warningAnim = rememberInfiniteTransition(label = "warningPulse")
                                val warningAlpha by warningAnim.animateFloat(
                                    initialValue = 0.5f,
                                    targetValue = 1.0f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(500),
                                        repeatMode = RepeatMode.Reverse
                                    ),
                                    label = "warningPulse"
                                )
                                
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Overdue task",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier
                                        .padding(start = 8.dp)
                                        .alpha(warningAlpha)
                                )
                            } else if (isDueSoon) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Due soon",
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                        
                        if (task.description.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            val descriptionOpacity by animateFloatAsState(
                                targetValue = if (isExpanded) 1f else 0.7f,
                                label = "descOpacity"
                            )
                            
                            Text(
                                text = task.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = descriptionOpacity),
                                maxLines = if (isExpanded) Int.MAX_VALUE else 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        
                        // Due date with animation
                        task.dueDate?.let {
                            val timeStr = task.dueTime?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: ""
                            val dateStr = it.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
                            val formattedDate = if (timeStr.isNotEmpty()) "$dateStr at $timeStr" else dateStr
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val dateColor by animateColorAsState(
                                    targetValue = when {
                                        isOverdue -> MaterialTheme.colorScheme.error
                                        isDueSoon -> MaterialTheme.colorScheme.tertiary
                                        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    },
                                    label = "dateColor"
                                )
                                
                                Text(
                                    text = "Due: $formattedDate",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = dateColor
                                )
                            }
                        }
                    }
                    
                    IconButton(
                        onClick = { isExpanded = !isExpanded }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = if (isExpanded) "Collapse" else "Expand",
                            modifier = Modifier
                                .rotate(arrowRotation)
                                .size(24.dp)
                        )
                    }
                }
                
                // Show subtasks if expanded and parent has subtasks
                AnimatedVisibility(
                    visible = isExpanded && subtasks.isNotEmpty(),
                    enter = expandVertically(
                        animationSpec = tween(150, easing = LinearOutSlowInEasing)
                    ) + fadeIn(
                        animationSpec = tween(150)
                    ),
                    exit = shrinkVertically(
                        animationSpec = tween(150, easing = LinearOutSlowInEasing)
                    ) + fadeOut(
                        animationSpec = tween(100)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        Text(
                            text = "Subtasks",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        
                        subtasks.forEach { subtask ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                
                                Text(
                                    text = subtask.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    textDecoration = if (subtask.isCompleted) lineThrough else none,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 8.dp)
                                )
                                
                                if (onCompleteSubtask != null && !subtask.isCompleted) {
                                    IconButton(
                                        onClick = { onCompleteSubtask(subtask) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Complete subtask",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = expandVertically(
                        animationSpec = tween(150, easing = LinearOutSlowInEasing)
                    ) + fadeIn(
                        animationSpec = tween(150)
                    ),
                    exit = shrinkVertically(
                        animationSpec = tween(150, easing = LinearOutSlowInEasing)
                    ) + fadeOut(
                        animationSpec = tween(100)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        if (!task.isCompleted) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Add subtask button (if not a subtask already)
                                if (!task.isSubtask() && onAddSubtask != null) {
                                    // Add button with pulsing animation
                                    val addButtonAnim = rememberInfiniteTransition(label = "addButtonPulse")
                                    val addButtonScale by addButtonAnim.animateFloat(
                                        initialValue = 1f,
                                        targetValue = 1.1f,
                                        animationSpec = infiniteRepeatable(
                                            animation = tween(1000),
                                            repeatMode = RepeatMode.Reverse
                                        ),
                                        label = "addButtonPulse"
                                    )
                                    
                                    IconButton(
                                        onClick = { onAddSubtask(task) },
                                        modifier = Modifier.scale(addButtonScale)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Add subtask",
                                            tint = MaterialTheme.colorScheme.tertiary
                                        )
                                    }
                                }
                                
                                // Check button with success animation
                                IconButton(
                                    onClick = {
                                        scope.launch {
                                            isCompleting = true
                                            delay(150)
                                            onComplete()
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Complete task",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                
                                // Delete button
                                IconButton(
                                    onClick = {
                                        scope.launch {
                                            isCompleting = true
                                            delay(150)
                                            onDelete()
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete task",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
} 