# PDA App — Roadmap

*Updated: 2026-03-28*

---

## UI Rework (Current Sprint)

### Design System
- Glassmorphism / frosted glass cards with translucent surfaces
- Vault-themed dark palette: deep charcoal backgrounds, amber glow primary, teal secondary
- Custom typography: Share Tech Mono (headings/monospace) + Inter (body)
- Shape tokens for consistent rounded corners

### Animation Libraries
- **Lottie for Compose** — task complete celebration, level-up fanfare, empty-state idle
- **Konfetti** — confetti burst on achievements and level-ups
- **Compose SharedTransitionLayout** (built-in, BOM 2024.09) — shared element transitions
- **Native Compose** — all micro-interactions (button press, card expand, list slide-in)
- **Android SoundPool** — pop on tap, swoosh on swipe, chime on task complete

### Component Redesign
- TaskItem: glass card with left category stripe, difficulty badge, swipe-to-complete/delete
- NpcMessageItem: chat bubble style, avatar circle
- MainScreen: compact ProfileStrip instead of large profile card, proper FAB
- All screens: consistent vault theme with amber/teal accents

---

## Bug Fixes (In Progress)
- ❌ → ✅ categoryLevels recalculation in awardXpForTask()
- ❌ → ✅ Sound system (SoundPool replacing ToneGenerator hack)
- ⚠️ → ✅ NPC message persistence
- ⚠️ → ✅ AnimatedBackground gradient coords

---

## Phase 3 — Post-Rework Features

### Gameplay Systems
- Task streaks: weekly streak bonuses + streak shield items
- Achievement system: first task, 7-day streak, category mastery milestones
- Anomaly events: time-limited bonus XP challenges
- Artifact system: passive bonuses earned from difficult tasks

### Navigation
- Swipe gestures (in-progress as part of UI rework)
- Visual calendar / timeline view
- Kanban board option

### NPC System
- NPC message persistence across app restarts
- Context-aware responses (reference user's task history)
- More NPCs and expanded dialogue

---

## Phase 4 — Long-Term

- AI-generated NPC dialogue (Gemini API free tier, offline fallback)
- Cloud sync (Firebase or custom backend)
- Different PDA skins (STALKER, Fallout Pip-Boy, Cyberpunk)
- Skill trees per category
- Faction system
- Widgets for home screen

---

## Architecture Goals
- Room database to replace SharedPreferences (cleaner queries, relationships)
- Repository layer with persistence for NPC messages
- Navigation component to replace boolean-flag push navigation
- Modularization into feature modules
- Expand test coverage beyond current (none)
