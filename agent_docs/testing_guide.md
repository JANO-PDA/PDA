# Testing Guide — PDA App

How to verify features work. Prefer Android Studio for builds and visual testing — only run Gradle CLI when explicitly needed.

---

## Build Commands

```bash
# Build debug APK (run only when explicitly asked)
./gradlew assembleDebug

# Run unit tests
./gradlew testDebugUnitTest

# Run lint
./gradlew lintDebug

# Clean build artifacts
./gradlew clean

# Full clean + build (use when cache issues suspected)
./gradlew clean assembleDebug
```

Prefer building and running in **Android Studio** — it gives visual feedback, logcat, and layout inspector that CLI cannot.

---

## Emulator / Device Checklist

Run through these after any significant change:

### Task Management
- [ ] Create task (all fields: title, description, difficulty, category, due date + time, reminder)
- [ ] Edit existing task — fields pre-populate correctly
- [ ] Complete task — XP increments, confetti fires, NPC message appears
- [ ] Delete task — XP decrements, NPC failure message appears
- [ ] Add subtask to a parent task — subtask shows under parent
- [ ] Complete subtask — partial XP awarded

### Filtering & Sorting
- [ ] Filter by each category — list shows only matching tasks
- [ ] Sort by Due Date — tasks with soonest due date appear first
- [ ] Sort by Difficulty — NIGHTMARE → HARD → MEDIUM → EASY order
- [ ] Default sort — tasks appear in creation order

### XP & Leveling
- [ ] Complete tasks until level-up occurs — level-up overlay fires
- [ ] XP bar fills correctly and animates
- [ ] Category progress screen shows updated XP per category
- [ ] Category level reflects task count (known bug: may be stuck at Level 1 — see known bugs)

### NPC System
- [ ] Contacts screen shows NPC messages after completing tasks
- [ ] Unread badge count appears on the message icon in TopBar
- [ ] Opening ContactsScreen marks all messages as read, badge clears
- [ ] Messages are categorized correctly (NPC from correct category appears)

### Streaks
- [ ] Streak counter increments when a task is completed on a new day
- [ ] Streak-at-risk warning appears (wobble animation, red flash) when streak may break

### Theme & Appearance
- [ ] Switch between ZONE_EXPLORER / RADIATION / PRIPYAT themes in Settings
- [ ] Toggle dark mode / light mode / system default
- [ ] Theme persists after app restart

### Notifications
- [ ] Create task with reminder enabled — notification fires at due time
- [ ] Permission prompt appears on Android 13+ for POST_NOTIFICATIONS
- [ ] Permission prompt appears on Android 12+ for SCHEDULE_EXACT_ALARM

---

## Known Broken — Re-test After Fixes

| Feature | Expected | Known Issue |
|---|---|---|
| Category level display | Shows correct level per category | Always shows Level 1 — `categoryLevels` not recalculated in `awardXpForTask()` |
| Alarm cancellation | Cancels alarm when task deleted | `Math.abs()` missing — may fail for negative hash codes |
| AnimatedBackground | Smooth dual-glow gradient animation | Gradient uses wrong coordinate scale (fractional vs pixels) |
| NPC messages on restart | Messages persist between sessions | In-memory only — lost on app restart |

---

## Lint Known Issues

Run `./gradlew lintDebug` and expect warnings in these areas:

- **Google Fonts network calls** — flagged as potential slow startup (acceptable for this project)
- **Missing content descriptions** — some icon-only buttons lack `contentDescription` — accessibility gap
- **Unused imports** from deleted files — safe to clean up

---

## What Does NOT Have Tests

Current unit test coverage is minimal (boilerplate removed in PR #3). Areas without tests:

- XP calculation logic (`UserProfile.calculateLevel`, `calculateXpForNextLevel`)
- Streak tracking (`TodoViewModel.updateStreak`)
- Task storage serialization/deserialization (`TaskStorage`)
- Category rank progression (`CategoryRank`)

If writing tests, these are the highest-value targets. Use `./gradlew testDebugUnitTest` to run.
