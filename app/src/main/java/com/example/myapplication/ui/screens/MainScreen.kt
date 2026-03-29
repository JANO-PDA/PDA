package com.example.myapplication.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.models.*
import com.example.myapplication.ui.components.*
import com.example.myapplication.ui.theme.*
import com.example.myapplication.ui.viewmodel.TodoViewModel
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

enum class SortOrder(val label: String) {
    DEFAULT("Default"),
    DUE_DATE("Due Date"),
    DIFFICULTY("Difficulty")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: TodoViewModel) {
    val tasks         by viewModel.tasks.collectAsState()
    val userProfile   by viewModel.userProfile.collectAsState()
    val showConfetti  by viewModel.showConfetti.collectAsState()
    val showLevelUp   by viewModel.showLevelUp.collectAsState()
    val addSubtaskFor by viewModel.addSubtaskFor.collectAsState()

    var showAddTaskDialog    by rememberSaveable { mutableStateOf(false) }
    var showCompletedTasks   by rememberSaveable { mutableStateOf(false) }
    var showCategoryProgress by rememberSaveable { mutableStateOf(false) }
    var showContactsScreen   by rememberSaveable { mutableStateOf(false) }
    var showSettingsScreen   by rememberSaveable { mutableStateOf(false) }

    var selectedCategory by remember { mutableStateOf<TaskCategory?>(null) }
    var sortOrder        by remember { mutableStateOf(SortOrder.DEFAULT) }
    var showSortMenu     by remember { mutableStateOf(false) }
    var editingTask      by remember { mutableStateOf<Task?>(null) }

    val drawerState  = rememberDrawerState(DrawerValue.Closed)
    val scope        = rememberCoroutineScope()
    val isDrawerOpen by remember { derivedStateOf { drawerState.currentValue == DrawerValue.Open } }
    val haptic       = LocalHapticFeedback.current

    // Sub-screen routing
    if (showContactsScreen)   { ContactsScreen(viewModel,        onNavigateBack = { showContactsScreen   = false }); return }
    if (showCompletedTasks)   { CompletedTasksScreen(viewModel,  onNavigateBack = { showCompletedTasks   = false }); return }
    if (showCategoryProgress) { CategoryProgressScreen(viewModel,onNavigateBack = { showCategoryProgress = false }); return }
    if (showSettingsScreen)   { SettingsScreen(viewModel,        onNavigateBack = { showSettingsScreen   = false }); return }

    val topLevelActive = tasks.filter { it.parentTaskId == null && !it.isCompleted }
    val subtaskMap     = tasks.filter { it.parentTaskId != null }.groupBy { it.parentTaskId!! }

    val filteredTasks = if (selectedCategory != null)
        topLevelActive.filter { it.category == selectedCategory } else topLevelActive

    val displayTasks = when (sortOrder) {
        SortOrder.DEFAULT    -> filteredTasks
        SortOrder.DUE_DATE   -> filteredTasks.sortedWith(compareBy(nullsLast()) { it.dueDate })
        SortOrder.DIFFICULTY -> filteredTasks.sortedByDescending { it.difficulty.ordinal }
    }

    ModalNavigationDrawer(
        drawerState   = drawerState,
        drawerContent = {
            ModalDrawerSheet(modifier = Modifier.width(280.dp)) {
                Spacer(Modifier.height(24.dp))
                Text(
                    "PDA",
                    style      = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier   = Modifier.padding(horizontal = 24.dp)
                )
                Text(
                    "Personal Device Assistant",
                    style    = MaterialTheme.typography.bodySmall,
                    color    = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                Spacer(Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(Modifier.height(8.dp))
                NavigationDrawerItem(
                    icon     = { Icon(Icons.Default.CheckCircle, null) },
                    label    = { Text("Completed Tasks") },
                    badge    = {
                        val n = tasks.count { it.isCompleted }
                        if (n > 0) Badge { Text(n.toString()) }
                    },
                    selected = false,
                    onClick  = { scope.launch { drawerState.close(); showCompletedTasks = true } },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                NavigationDrawerItem(
                    icon     = { Icon(Icons.Default.Analytics, null) },
                    label    = { Text("Category Stats") },
                    selected = false,
                    onClick  = { scope.launch { drawerState.close(); showCategoryProgress = true } },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                NavigationDrawerItem(
                    icon     = { Icon(Icons.Default.Settings, null) },
                    label    = { Text("Settings") },
                    selected = false,
                    onClick  = { scope.launch { drawerState.close(); showSettingsScreen = true } },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }
        }
    ) {
        AnimatedBackground {
            Scaffold(
                containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0f),
                topBar = {
                    TopAppBar(
                        title  = { Text("Tasks", style = MaterialTheme.typography.headlineSmall) },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
                        ),
                        navigationIcon = {
                            if (!isDrawerOpen) {
                                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                    Icon(Icons.Default.Menu, "Menu")
                                }
                            }
                        },
                        actions = {
                            IconButton(onClick = { showContactsScreen = true }) {
                                BadgedBox(badge = {
                                    val n = viewModel.getUnreadMessageCount()
                                    if (n > 0) Badge { Text(n.toString()) }
                                }) {
                                    Icon(Icons.AutoMirrored.Filled.Message, "Messages")
                                }
                            }
                        }
                    )
                },
                floatingActionButton = {
                    ExtendedFloatingActionButton(
                        onClick = {
                            showAddTaskDialog = true
                            viewModel.playTapSound()
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        },
                        icon           = { Icon(Icons.Default.Add, null) },
                        text           = { Text("Add Task") },
                        shape          = GlassPill,
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor   = MaterialTheme.colorScheme.onPrimary
                    )
                }
            ) { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp)
                ) {
                    Spacer(Modifier.height(12.dp))

                    GamerIDCard(userProfile = userProfile)

                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Active Tasks",
                            style      = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Box {
                            IconButton(onClick = { showSortMenu = true }) {
                                Icon(Icons.Default.Sort, "Sort")
                            }
                            DropdownMenu(
                                expanded         = showSortMenu,
                                onDismissRequest = { showSortMenu = false }
                            ) {
                                SortOrder.entries.forEach { order ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                order.label,
                                                fontWeight = if (sortOrder == order) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        onClick = { sortOrder = order; showSortMenu = false }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = selectedCategory == null,
                            onClick  = { selectedCategory = null },
                            label    = { Text("All") },
                            shape    = GlassPill
                        )
                        TaskCategory.entries.forEach { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick  = { selectedCategory = if (selectedCategory == cat) null else cat },
                                label    = {
                                    Row(
                                        verticalAlignment     = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(AppIcons.getCategoryIcon(cat), null, Modifier.size(12.dp))
                                        Text(cat.name.lowercase().replaceFirstChar { it.uppercase() })
                                    }
                                },
                                shape = GlassPill
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    if (displayTasks.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(bottom = 88.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            VaultEmptyState(
                                selectedCategory = selectedCategory,
                                onAddTask        = { showAddTaskDialog = true }
                            )
                        }
                    } else {
                        TaskList(
                            tasks          = displayTasks,
                            subtaskMap     = subtaskMap,
                            onTaskClick    = { viewModel.highlightTask(it.id) },
                            onTaskComplete = { viewModel.completeTask(it) },
                            onAddSubtask   = { viewModel.setAddSubtaskFor(it) },
                            onTaskEdit     = { editingTask = it },
                            viewModel      = viewModel,
                            modifier       = Modifier
                                .weight(1f)
                                .padding(bottom = 88.dp)
                        )
                    }
                }
            }

            if (showConfetti) ConfettiAnimation()
            LevelUpOverlay(visible = showLevelUp) { viewModel.dismissLevelUp() }
        }
    }

    // Dialogs / sheets
    if (showAddTaskDialog) {
        AddTaskDialog(
            onDismiss   = { showAddTaskDialog = false },
            onAddTask   = { title, desc, diff, cat, date, time, reminder ->
                viewModel.addTask(title, desc, diff, cat, date, time, reminder)
                showAddTaskDialog = false
            },
            onTapSound  = { viewModel.playTapSound() },
            onOpenSound = { viewModel.playSwipeSound() }
        )
    }

    editingTask?.let { task ->
        AddTaskDialog(
            existingTask = task,
            onDismiss    = { editingTask = null },
            onAddTask    = { title, desc, diff, cat, date, time, reminder ->
                viewModel.updateTask(
                    task.copy(
                        title = title, description = desc, difficulty = diff,
                        category = cat, dueDate = date, dueTime = time, hasReminder = reminder
                    )
                )
                editingTask = null
            },
            onTapSound  = { viewModel.playTapSound() },
            onOpenSound = { viewModel.playSwipeSound() }
        )
    }

    if (addSubtaskFor != null) {
        AddSubtaskDialog(
            parentTask   = addSubtaskFor!!,
            onDismiss    = { viewModel.dismissAddSubtaskDialog() },
            onAddSubtask = { t, d, diff ->
                viewModel.addSubtask(addSubtaskFor!!.id, t, d, diff)
                viewModel.dismissAddSubtaskDialog()
            }
        )
    }
}

// ─── Gamer ID Card ─────────────────────────────────────────────────────────────
@Composable
private fun GamerIDCard(userProfile: UserProfile) {
    val infiniteTransition = rememberInfiniteTransition(label = "gamerCard")

    val primary   = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val tertiary  = MaterialTheme.colorScheme.tertiary
    val error     = MaterialTheme.colorScheme.error
    val onSurface = MaterialTheme.colorScheme.onSurface
    val density   = LocalDensity.current

    // ── Streak-at-risk detection ───────────────────────────────────────────
    val streakAtRisk = remember(userProfile.lastCompletedTaskDate, userProfile.taskStreak) {
        if (userProfile.taskStreak <= 0) false
        else {
            val lastMs = userProfile.lastCompletedTaskDate ?: return@remember true
            val cal     = java.util.Calendar.getInstance()
            val todayCal = java.util.Calendar.getInstance()
            cal.timeInMillis = lastMs
            !(cal.get(java.util.Calendar.DAY_OF_YEAR) == todayCal.get(java.util.Calendar.DAY_OF_YEAR) &&
              cal.get(java.util.Calendar.YEAR) == todayCal.get(java.util.Calendar.YEAR))
        }
    }

    // ── Badge border glow pulse ────────────────────────────────────────────
    val badgeGlow by infiniteTransition.animateColor(
        initialValue  = primary.copy(alpha = 0.4f),
        targetValue   = primary,
        animationSpec = infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label         = "badgeGlow"
    )

    // ── CRT scan line (0→1 = top to bottom) ───────────────────────────────
    val scanY by infiniteTransition.animateFloat(
        initialValue  = 0f,
        targetValue   = 1f,
        animationSpec = infiniteRepeatable(tween(2200, easing = LinearEasing), RepeatMode.Restart),
        label         = "scanY"
    )

    // ── XP bar shimmer ─────────────────────────────────────────────────────
    val shimmerPos by infiniteTransition.animateFloat(
        initialValue  = -1f,
        targetValue   = 2f,
        animationSpec = infiniteRepeatable(tween(2500, easing = LinearEasing), RepeatMode.Restart),
        label         = "shimmer"
    )

    // ── Animated XP bar progress ───────────────────────────────────────────
    val xpProgress   = calculateProgressToNextLevel(userProfile.totalXp)
    val animXpFill by animateFloatAsState(
        targetValue   = xpProgress,
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label         = "xpFill"
    )

    // ── XP count-up ────────────────────────────────────────────────────────
    val animXp by animateIntAsState(
        targetValue   = userProfile.totalXp,
        animationSpec = tween(900, easing = FastOutSlowInEasing),
        label         = "xpCount"
    )

    // ── Streak flame pulse ─────────────────────────────────────────────────
    val flamePulse by infiniteTransition.animateFloat(
        initialValue  = 1f,
        targetValue   = 1.2f,
        animationSpec = infiniteRepeatable(tween(700, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label         = "flamePulse"
    )

    // ── Streak wobble (fast oscillation, only applied when at-risk) ────────
    val wobbleX by infiniteTransition.animateFloat(
        initialValue  = -3f,
        targetValue   = 3f,
        animationSpec = infiniteRepeatable(tween(110, easing = LinearEasing), RepeatMode.Reverse),
        label         = "wobble"
    )

    // ── Streak color flash (same spec, target changes) ─────────────────────
    val streakFlash by infiniteTransition.animateColor(
        initialValue  = tertiary,
        targetValue   = if (streakAtRisk) error else tertiary,
        animationSpec = infiniteRepeatable(tween(500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label         = "streakFlash"
    )

    val rankTitle = levelToRankTitle(userProfile.level)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(GlassCard)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.90f))
            .border(BorderStroke(1.dp, badgeGlow.copy(alpha = 0.5f)), GlassCard)
            .drawBehind {
                // Faint terminal grid
                val gridPx  = 24.dp.toPx()
                val gridClr = primary.copy(alpha = 0.04f)
                var x = 0f
                while (x <= size.width)  { drawLine(gridClr, Offset(x, 0f), Offset(x, size.height), 0.5f); x += gridPx }
                var y = 0f
                while (y <= size.height) { drawLine(gridClr, Offset(0f, y), Offset(size.width, y),   0.5f); y += gridPx }
            }
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {

            // ── Level badge ────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
                    .border(BorderStroke(1.5.dp, badgeGlow), RoundedCornerShape(14.dp))
                    .drawBehind {
                        // CRT scan line sweeps top → bottom
                        drawLine(
                            color       = primary.copy(alpha = 0.4f),
                            start       = Offset(0f, size.height * scanY),
                            end         = Offset(size.width, size.height * scanY),
                            strokeWidth = 1.5f
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text       = "LV",
                        style      = MaterialTheme.typography.labelSmall,
                        color      = primary.copy(alpha = 0.7f),
                        fontFamily = ShareTechMonoFamily,
                        letterSpacing = 1.5.sp
                    )
                    AnimatedContent(
                        targetState = userProfile.level,
                        transitionSpec = {
                            (slideInVertically { it } + fadeIn()) togetherWith
                            (slideOutVertically { -it } + fadeOut())
                        },
                        label = "levelNum"
                    ) { lv ->
                        Text(
                            text       = "$lv",
                            style      = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color      = primary,
                            fontFamily = ShareTechMonoFamily
                        )
                    }
                }
            }

            Spacer(Modifier.width(14.dp))

            // ── Info column ────────────────────────────────────────────────
            Column(modifier = Modifier.weight(1f)) {

                // Rank title row + streak
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    AnimatedContent(
                        targetState = rankTitle,
                        transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) },
                        label = "rankTitle"
                    ) { title ->
                        Text(
                            text          = title,
                            style         = MaterialTheme.typography.labelLarge,
                            color         = primary,
                            fontWeight    = FontWeight.Bold,
                            fontFamily    = ShareTechMonoFamily,
                            letterSpacing = 2.sp
                        )
                    }

                    // Streak indicator
                    if (userProfile.taskStreak > 0) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.offset {
                                IntOffset((if (streakAtRisk) wobbleX else 0f).roundToInt(), 0)
                            }
                        ) {
                            Icon(
                                Icons.Default.LocalFireDepartment,
                                contentDescription = "Streak",
                                tint     = streakFlash,
                                modifier = Modifier
                                    .size(18.dp)
                                    .scale(flamePulse)
                            )
                            AnimatedContent(
                                targetState    = userProfile.taskStreak,
                                transitionSpec = {
                                    (slideInVertically { it } + fadeIn()) togetherWith
                                    (slideOutVertically { -it } + fadeOut())
                                },
                                label = "streakNum"
                            ) { s ->
                                Text(
                                    "$s",
                                    style      = MaterialTheme.typography.labelSmall,
                                    color      = streakFlash,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(7.dp))

                // XP bar with shimmer
                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val barWidthPx = with(density) { maxWidth.toPx() }
                    val fillBrush = Brush.horizontalGradient(colors = listOf(primary, secondary))
                    val shimmerBrush = Brush.linearGradient(
                        colors = listOf(
                            primary.copy(alpha = 0f),
                            secondary.copy(alpha = 0.7f),
                            primary.copy(alpha = 0f)
                        ),
                        start = Offset(shimmerPos * barWidthPx, 0f),
                        end   = Offset((shimmerPos + 0.45f) * barWidthPx, 0f)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(9.dp)
                            .clip(RoundedCornerShape(50))
                            .background(primary.copy(alpha = 0.12f))
                    ) {
                        // gradient fill
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(animXpFill.coerceIn(0f, 1f))
                                .height(9.dp)
                                .background(fillBrush)
                        )
                        // shimmer sweep over fill
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(animXpFill.coerceIn(0f, 1f))
                                .height(9.dp)
                                .background(shimmerBrush)
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))

                // XP count + "to next level"
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text      = "$animXp XP",
                        style     = MaterialTheme.typography.labelSmall,
                        color     = primary,
                        fontFamily = ShareTechMonoFamily
                    )
                    val xpToNext = calculateXpForNextLevel(userProfile.totalXp) - userProfile.totalXp
                    Text(
                        text  = "$xpToNext to Lv ${userProfile.level + 1}",
                        style = MaterialTheme.typography.labelSmall,
                        color = onSurface.copy(alpha = 0.4f)
                    )
                }

                // Streak-at-risk warning
                AnimatedVisibility(
                    visible = streakAtRisk && userProfile.taskStreak > 0,
                    enter   = fadeIn(tween(300)) + expandVertically(tween(300)),
                    exit    = fadeOut(tween(200)) + shrinkVertically(tween(200))
                ) {
                    Text(
                        text       = "⚠  Complete a task to keep your streak!",
                        style      = MaterialTheme.typography.labelSmall,
                        color      = error,
                        fontWeight = FontWeight.SemiBold,
                        modifier   = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

private fun levelToRankTitle(level: Int): String = when {
    level < 5  -> "RECRUIT"
    level < 10 -> "SURVIVOR"
    level < 20 -> "STALKER"
    level < 35 -> "RANGER"
    level < 50 -> "VETERAN"
    else       -> "LEGEND"
}
