# PDA App — Current State (Verified Against Source Code)

*Last Audited: 2026-03-28 — based on actual source code, not prior docs*

---

## What Actually Works

### Task Management ✅
- Create / read / update / delete tasks
- Title, description, difficulty (EASY/MEDIUM/HARD/NIGHTMARE), category (6 types)
- Due date + time picker
- Subtasks (add, complete, display nested)
- Task editing — AddTaskDialog accepts `existingTask` for pre-fill
- Category filter chips + sort (Default / Due Date / Difficulty)
- Overdue detection (checked every 60 seconds in MainActivity)

### User Profile & XP ✅
- XP awarded on task completion: EASY=10, MEDIUM=25, HARD=50, NIGHTMARE=100
- Subtasks award XP correctly
- Level progression with 20% exponential XP curve per level
- Streak tracking (consecutive-day completion)
- Profile persists across app restarts (SharedPreferences + Gson)

### NPC System ✅
- 12 NPCs across 6 categories (2 per category), rich post-apocalyptic dialogue
- Completion and failure messages generated and shown in ContactsScreen
- Unread message badge in TopBar
- Auto-marks messages read when ContactsScreen opens
- Message detail dialog

### Notifications ✅ (with permissions)
- AlarmManager + setExactAndAllowWhileIdle for task reminders
- NotificationHelper creates channels and shows styled notifications
- Permission handling for POST_NOTIFICATIONS (Android 13+) and SCHEDULE_EXACT_ALARM (Android 12+)
- TaskAlarmReceiver handles wake locks correctly

### Theme & Dark Mode ✅
- 3 themes: ZONE_EXPLORER (green), RADIATION (orange), PRIPYAT (blue)
- Each has separate light + dark color schemes
- Dark/Light/System mode toggle
- All persisted in UserProfile

### Navigation ✅
- ModalNavigationDrawer with Completed Tasks, Category Statistics, Settings
- TopBar message icon with unread badge → ContactsScreen
- All screens have proper BackHandler

### Animations (Native Compose) ✅
- Task card scale + alpha on complete, highlighted, overdue states
- AnimatedVisibility expand/collapse for subtasks and action buttons
- Confetti (custom Canvas, works but basic)
- AnimatedBackground (gradient, minor coordinate bug)
- FloatingElement (vertical float on UserProfileCard)
- PulsatingIcon (overdue warning)
- AnimatedContent for XP/Level changes in profile

---

## Known Bugs (Verified in Code)

### ❌ categoryLevels never recalculated in awardXpForTask()
`awardXpForTask()` in TodoViewModel.kt updates `categoryXp` but does NOT recalculate
`categoryLevels`. CategoryProgressScreen reads `categoryLevels` → always shows Level 1 for all
categories regardless of XP earned.
*(completeSubtask does recalculate correctly — only the main task path is broken)*

### ❌ Sound is broken
`playCompletionSound()` uses reflection to find `R$raw.task_complete` which doesn't exist.
Falls back to a single ToneGenerator beep. No real sound effects in the app.

### ⚠️ NPC messages not persisted
NpcRepository uses in-memory StateFlow only. Messages are lost when the app restarts.

### ⚠️ AnimatedBackground gradient coordinates wrong
`Brush.linearGradient` receives fractional values (−0.5 to +1.5) instead of pixel offsets.
Gradient renders but not as designed.

### ⚠️ Alarm cancellation may fail
`cancelTaskReminder()` uses raw `task.id.hashCode()` as requestCode without `Math.abs()`,
unlike scheduling which does protect against negative values. May fail to cancel on some task IDs.

---

## Dead Code (Never Executed)

- `MenuScreen.kt` — MainScreen uses its own ModalNavigationDrawer; MenuScreen is never called
- `UserProfileCard.kt` — MainScreen inlines its own profile card; this component is unused
- `NotificationTab.kt` — imported nowhere
- `ProfileScreen.kt` — called nowhere
- `SubtaskValues.kt` — global vars set in showAddSubtaskDialog() but never read by dialogs

---

## Architecture

- Kotlin + Jetpack Compose, MVVM
- `TodoViewModel` — central state coordinator (StateFlow)
- `NpcRepository` — NPC/message management (in-memory)
- `TaskStorage` / `UserProfileStorage` — SharedPreferences + Gson persistence
- `AlarmScheduler` / `NotificationHelper` / `TaskAlarmReceiver` — notification pipeline
- minSdk 26, targetSdk 35, Compose BOM 2024.09.00

---

## What Is NOT in the App (Despite Docs Saying Otherwise)

- ❌ "Paper folding animation for Study tasks" — **never existed in source code**
- ❌ "Sound effects implemented" — only a ToneGenerator beep fallback
- ❌ "Refined animation system" — standard native Compose only
- ❌ AI-generated NPC dialogue — not implemented
