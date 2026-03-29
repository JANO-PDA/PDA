package com.example.myapplication.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.models.Task
import com.example.myapplication.data.models.TaskCategory
import com.example.myapplication.data.models.TaskDifficulty
import com.example.myapplication.ui.theme.*
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

// ─── Category accent mapping ─────────────────────────────────────────────────
private fun categoryAccentColor(category: TaskCategory): Color = when (category) {
    TaskCategory.WORK     -> CategoryWork
    TaskCategory.STUDY    -> CategoryStudy
    TaskCategory.HEALTH   -> CategoryHealth
    TaskCategory.PERSONAL -> CategoryPersonal
    TaskCategory.SHOPPING -> CategoryShopping
    TaskCategory.OTHER    -> CategoryOther
}

// ─── Difficulty badge ────────────────────────────────────────────────────────
private fun difficultyColor(difficulty: TaskDifficulty): Color = when (difficulty) {
    TaskDifficulty.EASY      -> DiffEasy
    TaskDifficulty.MEDIUM    -> DiffMedium
    TaskDifficulty.HARD      -> DiffHard
    TaskDifficulty.NIGHTMARE -> DiffNightmare
}

@Composable
private fun DifficultyBadge(difficulty: TaskDifficulty) {
    val color = difficultyColor(difficulty)
    Box(
        modifier = Modifier
            .clip(GlassPill)
            .background(color.copy(alpha = 0.18f))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text  = difficulty.name,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// ─── Swipe action background ─────────────────────────────────────────────────
@Composable
private fun SwipeBackground(offsetX: Float) {
    val isSwipingRight = offsetX > 0

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(GlassCard)
            .background(
                if (isSwipingRight)
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
                else
                    MaterialTheme.colorScheme.error.copy(alpha = 0.85f)
            ),
        contentAlignment = if (isSwipingRight) Alignment.CenterStart else Alignment.CenterEnd
    ) {
        Icon(
            imageVector = if (isSwipingRight) Icons.Default.Check else Icons.Default.Delete,
            contentDescription = null,
            tint   = Color.White,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}

// ─── Main TaskItem ────────────────────────────────────────────────────────────
@Composable
fun TaskItem(
    task: Task,
    onComplete: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    subtasks: List<Task> = emptyList(),
    onAddSubtask: ((Task) -> Unit)? = null,
    onCompleteSubtask: ((Task) -> Unit)? = null,
    onEdit: (() -> Unit)? = null,
    level: Int = 0,
    isHighlighted: Boolean = false
) {
    var isExpanded    by remember { mutableStateOf(isHighlighted) }
    var isCompleting  by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Swipe state
    var offsetX by remember { mutableFloatStateOf(0f) }
    val swipeThreshold = 200f

    LaunchedEffect(isHighlighted) {
        if (isHighlighted) isExpanded = true
    }

    val animatedOffsetX by animateFloatAsState(
        targetValue  = offsetX,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "swipeOffset"
    )

    val cardAlpha by animateFloatAsState(
        targetValue  = if (isCompleting) 0f else 1f,
        animationSpec = tween(200),
        label = "cardAlpha"
    )

    val cardScale by animateFloatAsState(
        targetValue  = if (isCompleting) 0.9f else if (isHighlighted) 1.03f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "cardScale"
    )

    val arrowRotation by animateFloatAsState(
        targetValue  = if (isExpanded) 180f else 0f,
        animationSpec = tween(200, easing = FastOutSlowInEasing),
        label = "arrowRot"
    )

    val accent        = categoryAccentColor(task.category)
    val isOverdue     = task.isOverdue()
    val isDueSoon     = task.isDueSoon()

    val surfaceColor  = MaterialTheme.colorScheme.surface
    val cardBg by animateColorAsState(
        targetValue  = when {
            isHighlighted -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
            isOverdue     -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.85f)
            isDueSoon     -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f)
            else          -> surfaceColor
        },
        animationSpec = tween(150),
        label = "cardBg"
    )

    // Level-based indent for subtasks
    val indent = (level * 16).dp

    // Lottie complete animation state
    var showCompleteLottie by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth().padding(start = indent)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .scale(cardScale)
                .alpha(cardAlpha)
        ) {
            // Swipe reveal background (only for non-completed, non-subtask)
            if (!task.isCompleted && offsetX != 0f) {
                SwipeBackground(offsetX = animatedOffsetX)
            }

            // Card with left accent stripe
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset { IntOffset(animatedOffsetX.roundToInt(), 0) }
                    .clip(GlassCard)
                    .background(cardBg)
                    .then(
                        if (!task.isCompleted) {
                            Modifier.pointerInput(task.id) {
                                detectHorizontalDragGestures(
                                    onDragEnd = {
                                        when {
                                            offsetX > swipeThreshold -> {
                                                scope.launch {
                                                    isCompleting = true
                                                    kotlinx.coroutines.delay(120)
                                                    onComplete()
                                                    offsetX = 0f
                                                    isCompleting = false
                                                }
                                            }
                                            offsetX < -swipeThreshold -> {
                                                scope.launch {
                                                    isCompleting = true
                                                    kotlinx.coroutines.delay(120)
                                                    onDelete()
                                                    offsetX = 0f
                                                    isCompleting = false
                                                }
                                            }
                                            else -> offsetX = 0f
                                        }
                                    },
                                    onDragCancel = { offsetX = 0f },
                                    onHorizontalDrag = { _, dragAmount ->
                                        offsetX = (offsetX + dragAmount).coerceIn(-swipeThreshold * 1.5f, swipeThreshold * 1.5f)
                                    }
                                )
                            }
                        } else Modifier
                    )
            ) {
                // Left accent stripe
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .fillMaxHeight()
                        .clip(GlassStripe)
                        .background(accent)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, end = 8.dp, top = 12.dp, bottom = 12.dp)
                ) {
                    // ─ Top row: icon + title + badges + expand arrow ─────────
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Category icon with optional urgency pulse
                        val pulseAnim = rememberInfiniteTransition(label = "iconPulse")
                        val iconScale by pulseAnim.animateFloat(
                            initialValue = 0.9f,
                            targetValue  = 1.1f,
                            animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Reverse),
                            label = "iconPulse"
                        )
                        Icon(
                            imageVector = AppIcons.getCategoryIcon(task.category),
                            contentDescription = null,
                            tint   = accent,
                            modifier = Modifier
                                .size(20.dp)
                                .scale(if (isOverdue || isDueSoon) iconScale else 1f)
                        )

                        Spacer(Modifier.width(8.dp))

                        // Title
                        Text(
                            text  = task.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (task.isCompleted)
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            else
                                MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        Spacer(Modifier.width(8.dp))

                        // Difficulty badge
                        if (!task.isCompleted) DifficultyBadge(task.difficulty)

                        // Overdue / due-soon icon
                        if (isOverdue) {
                            PulsatingIcon(
                                icon = Icons.Default.Warning,
                                contentDescription = "Overdue",
                                tint = MaterialTheme.colorScheme.error,
                                rotateEnabled = false,
                                pulseMinScale = 0.85f,
                                pulseMaxScale = 1.15f,
                                pulseDurationMs = 500,
                                modifier = Modifier.padding(start = 4.dp).size(18.dp)
                            )
                        } else if (isDueSoon) {
                            Icon(
                                Icons.Default.Schedule,
                                contentDescription = "Due soon",
                                tint   = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.padding(start = 4.dp).size(18.dp)
                            )
                        }

                        // Expand arrow
                        IconButton(
                            onClick = { isExpanded = !isExpanded },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.ExpandMore,
                                contentDescription = if (isExpanded) "Collapse" else "Expand",
                                modifier = Modifier.rotate(arrowRotation),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // ─ Description + due date (always visible, collapsed) ────
                    if (task.description.isNotEmpty()) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text  = task.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                            maxLines = if (isExpanded) Int.MAX_VALUE else 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    task.dueDate?.let { date ->
                        Spacer(Modifier.height(4.dp))
                        val timeStr = task.dueTime?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: ""
                        val dateStr = date.format(DateTimeFormatter.ofPattern("MMM dd"))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Schedule,
                                contentDescription = null,
                                tint = when {
                                    isOverdue -> MaterialTheme.colorScheme.error
                                    isDueSoon -> MaterialTheme.colorScheme.tertiary
                                    else      -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                },
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text  = if (timeStr.isNotEmpty()) "$dateStr · $timeStr" else dateStr,
                                style = MaterialTheme.typography.labelSmall,
                                color = when {
                                    isOverdue -> MaterialTheme.colorScheme.error
                                    isDueSoon -> MaterialTheme.colorScheme.tertiary
                                    else      -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                }
                            )
                        }
                    }

                    // ─ Expanded: subtasks + action buttons ───────────────────
                    AnimatedVisibility(
                        visible = isExpanded,
                        enter   = expandVertically(tween(160, easing = LinearOutSlowInEasing)) + fadeIn(tween(160)),
                        exit    = shrinkVertically(tween(120, easing = FastOutLinearInEasing)) + fadeOut(tween(100))
                    ) {
                        Column {
                            if (subtasks.isNotEmpty()) {
                                Spacer(Modifier.height(8.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text  = "Subtasks (${subtasks.count { it.isCompleted }}/${subtasks.size})",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(4.dp))
                                subtasks.forEach { subtask ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.SubdirectoryArrowRight,
                                            contentDescription = null,
                                            tint   = accent.copy(alpha = 0.6f),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            text  = subtask.title,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (subtask.isCompleted)
                                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                            else
                                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                            modifier = Modifier.weight(1f),
                                            textDecoration = if (subtask.isCompleted)
                                                androidx.compose.ui.text.style.TextDecoration.LineThrough
                                            else null
                                        )
                                        if (!subtask.isCompleted && onCompleteSubtask != null) {
                                            IconButton(
                                                onClick = { onCompleteSubtask(subtask) },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Check,
                                                    contentDescription = "Complete subtask",
                                                    tint = accent,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            if (!task.isCompleted) {
                                Spacer(Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Add subtask
                                    if (!task.isSubtask() && onAddSubtask != null) {
                                        TextButton(
                                            onClick = { onAddSubtask(task) },
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Icon(Icons.Default.Add, null, Modifier.size(14.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("Subtask", style = MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                    // Edit
                                    if (onEdit != null) {
                                        IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                                            Icon(
                                                Icons.Default.Edit,
                                                contentDescription = "Edit",
                                                tint = MaterialTheme.colorScheme.secondary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                    // Complete
                                    IconButton(
                                        onClick = {
                                            scope.launch {
                                                showCompleteLottie = true
                                                isCompleting = true
                                                kotlinx.coroutines.delay(150)
                                                onComplete()
                                            }
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = "Complete",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    // Delete
                                    IconButton(
                                        onClick = {
                                            scope.launch {
                                                isCompleting = true
                                                kotlinx.coroutines.delay(120)
                                                onDelete()
                                            }
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Lottie inline task-complete animation (overlaid on card)
            if (showCompleteLottie) {
                Box(
                    modifier = Modifier.matchParentSize(),
                    contentAlignment = Alignment.Center
                ) {
                    TaskCompleteLottie(
                        visible    = showCompleteLottie,
                        size       = 72.dp,
                        onFinished = { showCompleteLottie = false }
                    )
                }
            }
        }
    }
}

