# PDA App — Feature Status

*Audited: 2026-03-28 against actual source code*

Legend: ✅ Working | ⚠️ Partially working | ❌ Broken/Missing

---

## Phase 1 — Core Features
- ✅ Basic task management (create, edit, delete, complete)
- ✅ Task categories (WORK, STUDY, HEALTH, PERSONAL, SHOPPING, OTHER)
- ✅ XP system for completed tasks (10/25/50/100 by difficulty)
- ✅ User profile with level progression (exponential curve)
- ✅ Theme customization (3 themes, persisted)

## Phase 2 — In Progress
- ✅ Progress bars for each category (CategoryProgressScreen)
- ✅ Due dates and time picker
- ⚠️ Notifications — AlarmManager works but requires 2 runtime permissions (Android 12+/13+)
- ✅ Task completion rates per category
- ✅ Subtasks for complex goals
- ✅ Dark mode (system default / light / dark — all 3 work)
- ❌ Sound effects — ToneGenerator beep only; real sound resource missing

## Phase 3 — Planned
- ❌ Different PDA skins (STALKER, Pip-Boy, Cyberpunk)
- ❌ Task linking (complete one to unlock another)
- ✅ Daily/weekly task streaks (streak tracking implemented)
- ❌ Productivity trend graphs

## Phase 4 — Future Enhancements
- ❌ Anomaly events with XP rewards
- ❌ Inventory and Artifacts system
- ❌ Achievement system (streak badges, category mastery)
- ❌ Faction system
- ❌ Unlockable UI elements
- ❌ Visual calendar
- ❌ Skill trees
- ❌ Time tracking for tasks in progress

---

## Navigation & Layout
- ✅ Navigation Drawer (ModalNavigationDrawer)
- ❌ FAB with expandable sub-actions (button is inline, not FAB)
- ❌ Swipe gestures for task actions
- ❌ Collapsible task groups by due date
- ❌ Pull-to-refresh

## Visual Feedback & Animations
- ⚠️ Task completion celebration (basic Canvas confetti — works but rough)
- ✅ Expand/collapse subtasks with AnimatedVisibility
- ✅ Pulsating icon for overdue tasks (PulsatingIcon)
- ✅ Category progress indicators
- ❌ Animated task priority indicators

## Task Management Features
- ✅ Task editing (AddTaskDialog with existingTask pre-fill)
- ✅ Category-based filtering
- ✅ Smart sorting (Default / Due Date / Difficulty)
- ❌ Task templates
- ❌ Drag-and-drop reordering
- ❌ Task priority levels

## Visual Enhancements
- ⚠️ Custom task card designs (category color tint — subtle, not fully themed)
- ✅ Category icons (Material Icons mapping)
- ❌ Glassmorphism / frosted glass UI
- ❌ Custom typography (uses system default font)
- ❌ Vault-themed color palette (current: Material3 defaults recolored)

## Animations (Honest Status)
- ⚠️ Task completion: basic Canvas confetti, no Lottie
- ❌ NPC message arrival animation
- ✅ Progress bars (animated via animateFloatAsState)
- ❌ Screen transitions (no shared element transitions)
- ✅ Category icon pulse for urgent tasks (PulsatingIcon)

---

## Known Bugs
- ❌ categoryLevels stuck at Level 1 in CategoryProgressScreen
- ❌ Sound broken (missing raw resource, beep fallback)
- ⚠️ NPC messages lost on app restart
- ⚠️ AnimatedBackground gradient math incorrect
