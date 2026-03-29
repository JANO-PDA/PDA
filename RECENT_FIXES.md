# PDA App — Recent Fixes & Changes

---

## PR #4 — Fix all checked features (merged)

*What the code actually shows was done in this PR:*

### Features Completed
- **Task editing**: AddTaskDialog now accepts `existingTask` for pre-fill; edit button in TaskItem
- **Category filter chips**: Horizontally scrollable filter row on MainScreen
- **Sort order**: Dropdown with Default / Due Date / Difficulty options
- **Streak tracking**: `updateStreak()` in TodoViewModel with consecutive-day logic
- **Category color tinting**: TaskItem cards get subtle background tint based on category
- **PulsatingIcon for overdue**: Extracted reusable pulsating icon component
- **Task highlighting**: Tap on task → brief highlight + expand (5s timeout)
- **Profile persistence fix**: UserProfile now loads and saves across app restarts
- **XP values unified**: EASY=10, MEDIUM=25, HARD=50, NIGHTMARE=100 everywhere
- **Confetti re-enabled**: showConfetti() correctly fires on task completion
- **Dark mode**: System/Light/Dark toggle added to SettingsScreen
- **Theme switching**: 3 themes × light/dark all work

### Bug Fixes
- Fixed notification early-delivery issue (removed debug immediate broadcast from scheduleTaskReminder)
- Fixed menu icon double-showing when drawer opens
- Fixed back navigation in ContactsScreen (was exiting app instead of returning)
- Replaced deprecated `Divider` with `HorizontalDivider`
- Replaced deprecated icon imports with AutoMirrored versions

---

## Known Remaining Bugs (NOT fixed in PR #4)

- **categoryLevels never recalculated** — CategoryProgressScreen always shows Level 1
- **Sound broken** — `R$raw.task_complete` resource doesn't exist; code falls back to ToneGenerator beep. "Sound effects implemented" in prior docs was incorrect.
- **NPC messages lost on restart** — in-memory only
- **AnimatedBackground gradient math** — uses fractional unit-circle values where pixel offsets are expected

---

## Previous History (from prior builds — accuracy not guaranteed)

The following was claimed in previous docs but **not verified** or **not found** in source:
- ~~"Paper folding animation for Study tasks"~~ — not present in any source file
- ~~"Simplified animation system migration"~~ — animations were always native Compose

*Note: Documentation prior to PR #4 was written by an AI assistant without code verification
and should not be trusted as accurate.*
