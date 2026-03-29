package com.example.myapplication.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.models.Task
import com.example.myapplication.data.models.TaskCategory
import com.example.myapplication.data.models.TaskDifficulty
import com.example.myapplication.ui.theme.*
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onAddTask: (String, String, TaskDifficulty, TaskCategory, LocalDate?, LocalTime?, Boolean) -> Unit,
    existingTask: Task? = null,
    onTapSound: () -> Unit = {},
    onOpenSound: () -> Unit = {}
) {
    val isEditMode = existingTask != null

    var title              by remember { mutableStateOf(existingTask?.title ?: "") }
    var description        by remember { mutableStateOf(existingTask?.description ?: "") }
    var selectedDifficulty by remember { mutableStateOf(existingTask?.difficulty ?: TaskDifficulty.MEDIUM) }
    var selectedCategory   by remember { mutableStateOf(existingTask?.category ?: TaskCategory.PERSONAL) }
    var hasDueDate         by remember { mutableStateOf(existingTask?.dueDate != null) }
    var selectedDate       by remember { mutableStateOf(existingTask?.dueDate ?: LocalDate.now().plusDays(1)) }
    var selectedTime       by remember { mutableStateOf(existingTask?.dueTime ?: LocalTime.of(9, 0)) }
    var hasReminder        by remember { mutableStateOf(existingTask?.hasReminder ?: false) }
    var showDatePicker     by remember { mutableStateOf(false) }
    var showTimePicker     by remember { mutableStateOf(false) }

    val dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    val haptic     = LocalHapticFeedback.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Stagger visibility flags
    var showTitleField  by remember { mutableStateOf(false) }
    var showDescField   by remember { mutableStateOf(false) }
    var showDueRow      by remember { mutableStateOf(false) }
    var showDiffHeader  by remember { mutableStateOf(false) }
    var showDiffCard0   by remember { mutableStateOf(false) }
    var showDiffCard1   by remember { mutableStateOf(false) }
    var showDiffCard2   by remember { mutableStateOf(false) }
    var showDiffCard3   by remember { mutableStateOf(false) }
    var showCatHeader   by remember { mutableStateOf(false) }
    var showCatRow0     by remember { mutableStateOf(false) }
    var showCatRow1     by remember { mutableStateOf(false) }
    var showCatRow2     by remember { mutableStateOf(false) }
    var showConfirmBtn  by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        onOpenSound()
        showTitleField = true
        delay(80);  showDescField  = true
        delay(80);  showDueRow     = true
        delay(80);  showDiffHeader = true
        delay(50);  showDiffCard0  = true
        delay(50);  showDiffCard1  = true
        delay(50);  showDiffCard2  = true
        delay(50);  showDiffCard3  = true
        delay(60);  showCatHeader  = true
        delay(40);  showCatRow0    = true
        delay(40);  showCatRow1    = true
        delay(40);  showCatRow2    = true
        delay(60);  showConfirmBtn = true
    }

    val enterAnim = fadeIn(tween(200)) + slideInVertically(tween(220)) { it / 3 }
    val exitAnim  = fadeOut(tween(150))

    val diffEntries = TaskDifficulty.entries
    val diffVisible = listOf(showDiffCard0, showDiffCard1, showDiffCard2, showDiffCard3)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        shape            = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor   = MaterialTheme.colorScheme.surface,
        dragHandle       = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // Sheet title
            Text(
                text     = if (isEditMode) "EDIT MISSION" else "NEW MISSION",
                style    = MaterialTheme.typography.headlineSmall,
                color    = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            )

            // Title field
            AnimatedVisibility(showTitleField, enter = enterAnim, exit = exitAnim) {
                OutlinedTextField(
                    value         = title,
                    onValueChange = { title = it },
                    label         = { Text("Title") },
                    placeholder   = { Text("Enter task title") },
                    modifier      = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    singleLine    = true
                )
            }

            // Description field
            AnimatedVisibility(showDescField, enter = enterAnim, exit = exitAnim) {
                OutlinedTextField(
                    value         = description,
                    onValueChange = { description = it },
                    label         = { Text("Description") },
                    placeholder   = { Text("Optional details...") },
                    modifier      = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    minLines      = 2
                )
            }

            // Due date row
            AnimatedVisibility(showDueRow, enter = enterAnim, exit = exitAnim) {
                Column(modifier = Modifier.padding(bottom = 16.dp)) {
                    Row(
                        modifier          = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = hasDueDate, onCheckedChange = { hasDueDate = it })
                        Icon(Icons.Default.DateRange, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Set due date", style = MaterialTheme.typography.bodyMedium)
                    }
                    if (hasDueDate) {
                        Row(
                            modifier              = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick  = { showDatePicker = true },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.DateRange, null, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    selectedDate.format(dateFormatter),
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                            OutlinedButton(
                                onClick  = { showTimePicker = true },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.AccessTime, null, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    selectedTime.format(timeFormatter),
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked       = hasReminder,
                                onCheckedChange = { hasReminder = it },
                                enabled       = hasDueDate
                            )
                            Icon(Icons.Default.Notifications, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Set reminder", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            // Difficulty header
            AnimatedVisibility(showDiffHeader, enter = enterAnim, exit = exitAnim) {
                Text(
                    "DIFFICULTY",
                    style    = MaterialTheme.typography.labelLarge,
                    color    = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Difficulty cards — 2-column grid
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Left column: EASY (0) + HARD (2)
                Column(
                    modifier            = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    listOf(0, 2).forEach { idx ->
                        AnimatedVisibility(diffVisible[idx], enter = enterAnim, exit = exitAnim) {
                            DifficultyCard(
                                difficulty = diffEntries[idx],
                                selected   = selectedDifficulty == diffEntries[idx],
                                onClick    = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onTapSound()
                                    selectedDifficulty = diffEntries[idx]
                                }
                            )
                        }
                    }
                }
                // Right column: MEDIUM (1) + NIGHTMARE (3)
                Column(
                    modifier            = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    listOf(1, 3).forEach { idx ->
                        AnimatedVisibility(diffVisible[idx], enter = enterAnim, exit = exitAnim) {
                            DifficultyCard(
                                difficulty = diffEntries[idx],
                                selected   = selectedDifficulty == diffEntries[idx],
                                onClick    = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onTapSound()
                                    selectedDifficulty = diffEntries[idx]
                                }
                            )
                        }
                    }
                }
            }

            // Category header
            AnimatedVisibility(showCatHeader, enter = enterAnim, exit = exitAnim) {
                Text(
                    "CATEGORY",
                    style    = MaterialTheme.typography.labelLarge,
                    color    = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Category rows — 2 per row
            val catRows = listOf(
                Triple(showCatRow0, TaskCategory.WORK,     TaskCategory.STUDY),
                Triple(showCatRow1, TaskCategory.HEALTH,   TaskCategory.PERSONAL),
                Triple(showCatRow2, TaskCategory.SHOPPING, TaskCategory.OTHER)
            )
            catRows.forEachIndexed { rowIdx, (visible, catA, catB) ->
                AnimatedVisibility(
                    visible = visible,
                    enter   = enterAnim,
                    exit    = exitAnim
                ) {
                    Row(
                        modifier              = Modifier
                            .fillMaxWidth()
                            .padding(bottom = if (rowIdx == catRows.lastIndex) 20.dp else 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        listOf(catA, catB).forEach { cat ->
                            CategoryCard(
                                category = cat,
                                selected = selectedCategory == cat,
                                onClick  = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onTapSound()
                                    selectedCategory = cat
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Confirm button
            AnimatedVisibility(showConfirmBtn, enter = enterAnim, exit = exitAnim) {
                val btnScale by animateFloatAsState(
                    targetValue   = if (title.isNotBlank()) 1.03f else 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness    = Spring.StiffnessLow
                    ),
                    label = "confirmBtnScale"
                )
                Button(
                    onClick  = {
                        if (title.isNotBlank()) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onTapSound()
                            onAddTask(
                                title, description, selectedDifficulty, selectedCategory,
                                if (hasDueDate) selectedDate else null,
                                if (hasDueDate) selectedTime else null,
                                hasReminder && hasDueDate
                            )
                            onDismiss()
                        }
                    },
                    enabled  = title.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .scale(btnScale)
                ) {
                    Text(if (isEditMode) "SAVE MISSION" else "DEPLOY MISSION")
                }
            }
        }
    }

    // Date picker overlay
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton    = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        selectedDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) { DatePicker(state = datePickerState) }
    }

    // Time picker overlay
    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour   = selectedTime.hour,
            initialMinute = selectedTime.minute
        )
        TimePickerDialog(
            onDismiss = { showTimePicker = false },
            onConfirm = {
                selectedTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                showTimePicker = false
            }
        ) { TimePicker(state = timePickerState) }
    }
}

// ─── Difficulty card ─────────────────────────────────────────────────────────

@Composable
private fun DifficultyCard(
    difficulty: TaskDifficulty,
    selected: Boolean,
    onClick: () -> Unit
) {
    val color   = difficultyAccent(difficulty)
    val xpLabel = difficultyXpLabel(difficulty)
    val icon    = difficultyIcon(difficulty)

    val scale by animateFloatAsState(
        targetValue   = if (selected) 1.04f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessMedium
        ),
        label = "diffCardScale"
    )
    val borderColor by animateColorAsState(
        targetValue   = if (selected) color else color.copy(alpha = 0.18f),
        animationSpec = tween(200),
        label         = "diffCardBorder"
    )
    val bgAlpha by animateFloatAsState(
        targetValue   = if (selected) 0.22f else 0.07f,
        animationSpec = tween(200),
        label         = "diffCardBg"
    )

    Card(
        onClick   = onClick,
        modifier  = Modifier.fillMaxWidth().scale(scale),
        shape     = GlassCard,
        border    = BorderStroke(if (selected) 1.5.dp else 1.dp, borderColor),
        colors    = CardDefaults.cardColors(containerColor = color.copy(alpha = bgAlpha)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector        = icon,
                contentDescription = null,
                tint               = color,
                modifier           = Modifier.size(22.dp)
            )
            Text(
                text       = difficulty.name,
                style      = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color      = color,
                textAlign  = TextAlign.Center
            )
            Text(
                text      = xpLabel,
                style     = MaterialTheme.typography.labelSmall,
                color     = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                textAlign = TextAlign.Center
            )
        }
    }
}

// ─── Category card ────────────────────────────────────────────────────────────

@Composable
private fun CategoryCard(
    category: TaskCategory,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val color = categoryAccent(category)

    val scale by animateFloatAsState(
        targetValue   = if (selected) 1.04f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessMedium
        ),
        label = "catCardScale"
    )
    val borderColor by animateColorAsState(
        targetValue   = if (selected) color else color.copy(alpha = 0.18f),
        animationSpec = tween(200),
        label         = "catCardBorder"
    )
    val bgAlpha by animateFloatAsState(
        targetValue   = if (selected) 0.22f else 0.07f,
        animationSpec = tween(200),
        label         = "catCardBg"
    )

    Card(
        onClick   = onClick,
        modifier  = modifier.scale(scale),
        shape     = GlassCard,
        border    = BorderStroke(if (selected) 1.5.dp else 1.dp, borderColor),
        colors    = CardDefaults.cardColors(containerColor = color.copy(alpha = bgAlpha)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector        = AppIcons.getCategoryIcon(category),
                contentDescription = null,
                tint               = color,
                modifier           = Modifier.size(20.dp)
            )
            Text(
                text       = category.name.lowercase().replaceFirstChar { it.uppercase() },
                style      = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color      = color,
                textAlign  = TextAlign.Center
            )
        }
    }
}

// ─── Pure helpers (no @Composable) ───────────────────────────────────────────

private fun difficultyAccent(difficulty: TaskDifficulty): Color = when (difficulty) {
    TaskDifficulty.EASY      -> DiffEasy
    TaskDifficulty.MEDIUM    -> DiffMedium
    TaskDifficulty.HARD      -> DiffHard
    TaskDifficulty.NIGHTMARE -> DiffNightmare
}

private fun difficultyXpLabel(difficulty: TaskDifficulty): String = when (difficulty) {
    TaskDifficulty.EASY      -> "+10 XP"
    TaskDifficulty.MEDIUM    -> "+25 XP"
    TaskDifficulty.HARD      -> "+50 XP"
    TaskDifficulty.NIGHTMARE -> "+100 XP"
}

private fun difficultyIcon(difficulty: TaskDifficulty): ImageVector = when (difficulty) {
    TaskDifficulty.EASY      -> Icons.Default.CheckCircle
    TaskDifficulty.MEDIUM    -> Icons.Default.Star
    TaskDifficulty.HARD      -> Icons.Default.Warning
    TaskDifficulty.NIGHTMARE -> Icons.Default.Error
}

private fun categoryAccent(category: TaskCategory): Color = when (category) {
    TaskCategory.WORK     -> CategoryWork
    TaskCategory.STUDY    -> CategoryStudy
    TaskCategory.HEALTH   -> CategoryHealth
    TaskCategory.PERSONAL -> CategoryPersonal
    TaskCategory.SHOPPING -> CategoryShopping
    TaskCategory.OTHER    -> CategoryOther
}
