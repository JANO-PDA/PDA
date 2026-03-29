# Architecture — PDA App

MVVM pattern, folder structure, data flow, and navigation. Read this before adding new files or wiring up new features.

---

## Pattern: MVVM + Compose

```
UI Layer (Composables)
    ↓ reads StateFlow, calls ViewModel methods
ViewModel Layer (TodoViewModel)
    ↓ orchestrates business logic, persistence, notifications
Data Layer (Storage, Repository, Models)
```

Single ViewModel (`TodoViewModel`) is the source of truth for all app state. No multiple ViewModels — keep it simple.

---

## Folder Structure

```
app/src/main/java/com/example/myapplication/
├── data/
│   ├── models/
│   │   ├── Task.kt               — Core task data class
│   │   ├── TaskCategory.kt       — Enum: WORK, STUDY, HEALTH, PERSONAL, SHOPPING, OTHER
│   │   ├── TaskDifficulty.kt     — Enum: EASY, MEDIUM, HARD, NIGHTMARE
│   │   ├── UserProfile.kt        — Player progression (XP, level, streaks, theme)
│   │   ├── NpcMessage.kt         — Npc + NpcMessage data classes
│   │   ├── CategoryRank.kt       — 10-tier rank system per category
│   │   └── AppTheme.kt           — Enum: ZONE_EXPLORER, RADIATION, PRIPYAT
│   ├── repository/
│   │   └── NpcRepository.kt      — In-memory NPC roster + message generation
│   ├── TaskStorage.kt            — SharedPreferences + Gson persistence for tasks
│   └── UserProfileStorage.kt     — SharedPreferences + Gson persistence for profile
├── notifications/
│   ├── AlarmScheduler.kt         — AlarmManager for task reminders
│   ├── NotificationHelper.kt     — Channel creation + notification building
│   ├── TaskAlarmReceiver.kt      — BroadcastReceiver for alarm events
│   └── PermissionHandler.kt      — POST_NOTIFICATIONS + SCHEDULE_EXACT_ALARM requests
├── ui/
│   ├── components/
│   │   ├── TaskItem.kt           — Expandable task card with swipe gestures
│   │   ├── TaskList.kt           — LazyColumn with category filter + sort
│   │   ├── AddTaskDialog.kt      — ModalBottomSheet: create/edit task
│   │   ├── AddSubtaskDialog.kt   — AlertDialog: add subtask to parent
│   │   ├── NpcMessageItem.kt     — Chat bubble message card
│   │   ├── ConfettiAnimation.kt  — Konfetti particle burst
│   │   ├── LottieOverlay.kt      — LevelUpOverlay, TaskCompleteLottie, EmptyStateLottie
│   │   ├── AnimatedBackground.kt — Dual-radial-glow gradient background
│   │   ├── PulsatingIcon.kt      — InfiniteTransition icon with scale + rotation
│   │   ├── FloatingElement.kt    — Vertical float animation wrapper
│   │   ├── TimePickerDialog.kt   — Material3 TimePicker in AlertDialog wrapper
│   │   └── VaultEmptyState.kt    — Animated vault door Canvas (empty state)
│   ├── screens/
│   │   ├── MainScreen.kt         — Primary hub: task list, GamerIDCard, drawer nav
│   │   ├── CategoryProgressScreen.kt — Per-category XP + rank bars
│   │   ├── CompletedTasksScreen.kt   — Grouped archive of completed tasks
│   │   ├── ContactsScreen.kt         — NPC message list (chat bubble style)
│   │   └── SettingsScreen.kt         — Theme + dark mode picker
│   ├── theme/
│   │   ├── Color.kt              — All color tokens
│   │   ├── Type.kt               — Share Tech Mono + Inter font families
│   │   ├── Shape.kt              — GlassCard, GlassPill, GlassDialog, GlassChip, GlassStripe
│   │   ├── Theme.kt              — 3 MaterialTheme color schemes (6 variants total)
│   │   ├── Animation.kt          — Reusable spring/tween/infinite specs
│   │   ├── SoundManager.kt       — SoundPool wrapper
│   │   ├── AppIcons.kt           — getCategoryIcon(), getRankIcon()
│   │   └── NpcAvatars.kt         — NPC avatar icon mapping by ID
│   └── viewmodel/
│       └── TodoViewModel.kt      — Single ViewModel, all app state
└── MainActivity.kt
```

---

## Dead Code — Do Not Touch

These files exist but are unused. Do not import, reference, or modify them:

| File | Reason unused |
|---|---|
| `MenuScreen.kt` | Replaced by `ModalNavigationDrawer` in `MainScreen.kt` |
| `UserProfileCard.kt` | Replaced by inline `GamerIDCard` in `MainScreen.kt` |
| `NotificationTab.kt` | Never imported anywhere |
| `ProfileScreen.kt` | Never called from navigation |
| `SubtaskValues.kt` | Global vars set but never read |

These are scheduled for removal. Do not build on them.

---

## State Flows (TodoViewModel)

```kotlin
val tasks: StateFlow<List<Task>>
val userProfile: StateFlow<UserProfile>
val npcMessages: StateFlow<List<NpcMessage>>   // from NpcRepository
val categoryStats: StateFlow<Map<TaskCategory, CategoryStat>>
val showConfetti: StateFlow<Boolean>
val showLevelUp: StateFlow<Boolean>
val isContactsScreenOpen: StateFlow<Boolean>
val highlightedTaskId: StateFlow<String?>      // for task navigation focus
```

Collect in composables with `collectAsStateWithLifecycle()`.

---

## Data Persistence

| Data | Storage | Notes |
|---|---|---|
| Tasks | `TaskStorage` → SharedPreferences + Gson | Persisted immediately on every mutation |
| UserProfile | `UserProfileStorage` → SharedPreferences + Gson | Persisted after XP, level, streak changes |
| NPC Messages | `NpcRepository` → in-memory `MutableStateFlow` | **Lost on app restart** — no persistence layer yet |
| Theme / dark mode | Inside `UserProfile` → `UserProfileStorage` | Part of profile persistence |

---

## Navigation

No `NavController` or Navigation Compose. Navigation is manual state in `MainScreen.kt`:

```kotlin
// Screens are shown/hidden via boolean state or isOpen flags
ModalNavigationDrawer { /* drawer content with screen links */ }

// Sub-screens are composables called conditionally
if (showCategoryProgress) CategoryProgressScreen(...)
if (showCompletedTasks) CompletedTasksScreen(...)
// etc.
```

---

## NPC Data Flow

```
NpcRepository
  ├── 12 hardcoded NPCs (2 per category)
  ├── generateCompletionMessage(category) → NpcMessage
  └── generateFailureMessage(category) → NpcMessage
        ↓ called by TodoViewModel.completeTask() / deleteTask()
TodoViewModel._npcMessages (StateFlow)
        ↓
ContactsScreen reads and displays
CompletedTasksScreen groups by category
MainScreen shows unread badge count
```

---

## XP + Level System

```
Task.getXpReward() → EASY=10, MEDIUM=25, HARD=50, NIGHTMARE=100
        ↓ TodoViewModel.awardXpForTask(task)
UserProfile.totalXp += reward
UserProfile.categoryXp[category] += reward
UserProfile.calculateLevel(totalXp) → new level
        ↓ if level changed
showLevelUp = true → LevelUpOverlay shown in MainScreen
showConfetti = true → ConfettiAnimation shown in MainScreen
```

Level thresholds: exponential growth, `calculateXpForNextLevel(level)` in `UserProfile.kt`.
Category level thresholds: based on task count, exponential in `CategoryRank.kt`.

---

## Known Bugs (as of 2026-03-28)

| Bug | Location | Notes |
|---|---|---|
| `categoryLevels` never recalculated | `TodoViewModel.awardXpForTask()` | `completeSubtask()` does it correctly — fix should mirror that |
| Alarm cancellation unreliable | `AlarmScheduler.kt` | Missing `Math.abs()` on `task.id.hashCode()` |
| `AnimatedBackground` gradient wrong scale | `AnimatedBackground.kt` | Uses fractional unit-circle values instead of pixel offsets |
| NPC messages lost on restart | `NpcRepository` | No persistence layer — needs Room or SharedPreferences |
