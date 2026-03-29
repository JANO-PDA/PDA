# CLAUDE.md — PDA App Design Guidelines

This file documents the design system, conventions, and rules for the PDA Android app.
Read this before touching any UI code. **For detailed guides, see `agent_docs/` folder.**

---

## Quick Reference

- **Build**: `./gradlew assembleDebug`
- **Test**: `./gradlew testDebugUnitTest`
- **Lint**: `./gradlew lintDebug`
- **Clean**: `./gradlew clean`
- **Prefer Android Studio** for building/testing — only run Gradle when explicitly asked (saves tokens, better feedback).

---

## Design Philosophy

- **Glassmorphism-first**: cards use semi-transparent surfaces (`surface.copy(alpha = 0.85–0.97f)`), never opaque fills
- **Vault post-apocalyptic accent**: amber terminal glow as primary, teal scanner as secondary, indigo as tertiary
- **Dark-primary**: the app is dark-mode-first; light theme is a `ParchmentBg` fallback
- **Everything animates**: static UI is a design failure. Entry animations, ambient pulses, and interaction feedback are mandatory

---

## Color Tokens

| Token | Value | Use |
|---|---|---|
| `AmberGlow` | `#F59E0B` | Primary — vault terminal amber, XP bars, primary actions |
| `TealAccent` | `#14B8A6` | Secondary — scanner line, shimmer shimmer accents |
| `IndigoHighlight` | `#6366F1` | Tertiary — rank progress bars, highlights |
| `ErrorRed` | `#EF4444` | Error states, streak-at-risk flash |
| `VaultBackground` | `#0A0A0F` | Root background |
| `VaultSurface` | `#14141C` | Card/sheet background |
| `DiffEasy` | `#4ADE80` | Easy difficulty |
| `DiffMedium` | `#FBBF24` | Medium difficulty |
| `DiffHard` | `#F97316` | Hard difficulty |
| `DiffNightmare` | `#EF4444` | Nightmare difficulty |
| `CategoryWork` | `#3B82F6` | Work category accent |
| `CategoryStudy` | `#22C55E` | Study category accent |
| `CategoryHealth` | `#EF4444` | Health category accent |
| `CategoryPersonal` | `#A855F7` | Personal category accent |
| `CategoryShopping` | `#F97316` | Shopping category accent |
| `CategoryOther` | `#94A3B8` | Other category accent |

---

## Typography

| Style | Font | Use |
|---|---|---|
| `headlineSmall–displayLarge` | Share Tech Mono | Section headers, dialog titles, vault terminal text |
| `titleMedium–titleLarge` | Inter SemiBold | Card titles, task names |
| `bodyMedium–bodyLarge` | Inter Regular | Descriptions, labels |
| `labelSmall–labelLarge` | Inter Medium | Chips, badges, stat labels |

**Rule**: Use `MaterialTheme.typography.*` — never hardcode `fontSize` or `fontFamily`.

---

## Shape Tokens

All defined in `ui/theme/Shape.kt`:

| Token | Value | Use |
|---|---|---|
| `GlassCard` | `RoundedCornerShape(16.dp)` | Task cards, stat cards |
| `GlassPill` | `RoundedCornerShape(50)` | Level badges, streak pill |
| `GlassDialog` | `RoundedCornerShape(24.dp)` | Dialogs, alert containers |
| `GlassChip` | `RoundedCornerShape(8.dp)` | Filter chips, small tags |

---

## Animation Rules

1. **Interactions** (tap, select, expand): use `spring()` — `DampingRatioMediumBouncy` + `StiffnessMedium`
2. **Data transitions** (XP bar fill, count-up): use `tween(1200, easing = FastOutSlowInEasing)`
3. **Ambient effects** (scan lines, shimmer, pulsing): use `InfiniteTransition` — never a coroutine loop
4. **Entry/exit visibility**: `AnimatedVisibility` with `fadeIn + slideInVertically` for sheets, `fadeIn + fadeOut` for overlays
5. **Stagger pattern**: `LaunchedEffect(Unit)` + sequential `delay()` calls, total ~700ms for full sheet reveal
6. **Never** use `animate*AsState` inside a loop body — create one per animated property

---

## Components

### Do NOT use
- `AlertDialog` for task creation/editing — use `ModalBottomSheet`
- `FilterChip` grids for difficulty/category selection — use `DifficultyCard` / `CategoryCard` from `AddTaskDialog.kt`
- `values()` enum method — always use `.entries`

### Do use
- `AnimatedBackground` wrapper for screens with moving gradient backgrounds
- `GamerIDCard` for the user profile display (replaces old `ProfileStrip`)
- `VaultEmptyState` for the empty task list state
- `GlassCard` / `GlassPill` / `GlassDialog` shapes everywhere

---

## Sound + Haptics

Every interactive element **must** pair visual feedback with:

1. **Haptic**: `LocalHapticFeedback.current`
   - Light tap (chip, card select): `HapticFeedbackType.TextHandleMove`
   - Confirm / submit: `HapticFeedbackType.LongPress`

2. **Sound**: via `SoundManager` (passed as lambdas from ViewModel)
   - Tap: `viewModel.playTapSound()`
   - Swipe/sheet open: `viewModel.playSwipeSound()`
   - Task complete: `viewModel.playCompleteSound()`
   - Level up: `viewModel.playLevelUpSound()`

> **Known gap**: `res/raw/` sound files are XML placeholders, not real `.ogg` files. Sound callbacks exist but produce no audio until real files are added.

---

## XP System

| Difficulty | XP reward |
|---|---|
| EASY | 10 XP |
| MEDIUM | 25 XP |
| HARD | 50 XP |
| NIGHTMARE | 100 XP |

Level thresholds: defined in `data/models/UserProfile.kt` via `calculateXpForNextLevel(level)`.

---

## Code Conventions

- **No AlertDialog** — use `ModalBottomSheet`
- **No deprecated `values()`** — use `.entries`
- **No `graphicsLayer { rotationZ = X }`** — use `Modifier.rotate(X)` from `androidx.compose.ui.draw`
- **Import specificity**: when `FastOutSlowInEasing` is ambiguous, add explicit `import androidx.compose.animation.core.FastOutSlowInEasing`
- **Canvas helpers**: put per-DrawScope logic in private `DrawScope.drawXxx()` extension functions
- **No private `Modifier` extensions** that shadow stdlib — use the Compose built-in `Modifier.rotate`, `Modifier.scale`, etc.
- **Streak at-risk**: compare `Calendar.DAY_OF_YEAR` + `Calendar.YEAR` against `userProfile.lastCompletedTaskDate` (stored as `Long?` milliseconds)
- **Always update** `CURRENT_STATE.md` and `FEATURES.md` when changing features
- **Always verify** builds pass before committing
- **Dead code — do not touch**: `MenuScreen`, `UserProfileCard`, `NotificationTab`, `ProfileScreen`, `SubtaskValues` — legacy files scheduled for removal
