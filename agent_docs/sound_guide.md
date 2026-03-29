# Sound Guide — PDA App

How the sound system works, what triggers what, and what's needed to make it fully functional.

---

## Current Status

> **Gap**: `res/raw/` sound files are XML placeholders, not real audio. The SoundPool framework is fully wired — sounds will play the moment real `.ogg` files replace the placeholders.

---

## SoundManager — `ui/theme/SoundManager.kt`

Wraps Android's `SoundPool` for low-latency game-style audio.

```kotlin
// Initialization (done in TodoViewModel.initialize())
val soundManager = SoundManager(context)

// The 4 available triggers
soundManager.playTap()       // Button tap, chip select
soundManager.playSwipe()     // Sheet open, swipe gesture
soundManager.playComplete()  // Task or subtask completion
soundManager.playLevelUp()   // Player leveled up
```

AudioAttributes config: `USAGE_GAME` + `CONTENT_TYPE_SONIFICATION` — bypasses media volume, uses notification volume.

---

## ViewModel Integration

Sound is called via ViewModel methods — never call `SoundManager` directly from UI composables.

```kotlin
// From any Composable
viewModel.playTapSound()
viewModel.playSwipeSound()
viewModel.playCompleteSound()   // called automatically in completeTask()
viewModel.playLevelUpSound()    // called automatically on level-up
```

Tap and swipe sounds need to be called manually from UI interaction handlers (FAB click, chip select, sheet open gesture).

---

## Required Sound Files

Place real audio files at these paths (replace the current XML placeholders):

| File | Path | Trigger | Character |
|---|---|---|---|
| `task_complete.ogg` | `app/src/main/res/raw/task_complete.ogg` | Task / subtask completed | Short success ding, ~0.5s |
| `button_tap.ogg` | `app/src/main/res/raw/button_tap.ogg` | Button tap, chip select | Soft pop, ~0.1s |
| `swipe_action.ogg` | `app/src/main/res/raw/swipe_action.ogg` | Swipe gesture, sheet open | Quick swoosh, ~0.2s |
| `level_up.ogg` | `app/src/main/res/raw/level_up.ogg` | Player levels up | Fanfare / chime, ~1.5–2s |

---

## Audio File Specs

| Property | Requirement |
|---|---|
| Format | `.ogg` (Vorbis) — best Android compatibility |
| Sample rate | 44100 Hz |
| Channels | Mono (tap/swipe) or Stereo (level-up) |
| Bit depth | 16-bit |
| Max file size | < 50KB for short SFX, < 200KB for level-up |

OGG Vorbis encodes smaller than MP3 at equivalent quality and is the preferred format for Android game audio.

---

## Haptic Pairing

Every sound trigger must be paired with haptic feedback (both are required per CLAUDE.md):

| Interaction | Sound | Haptic |
|---|---|---|
| Chip / card select | `playTapSound()` | `HapticFeedbackType.TextHandleMove` |
| FAB / submit button | `playTapSound()` | `HapticFeedbackType.LongPress` |
| Swipe gesture | `playSwipeSound()` | `HapticFeedbackType.TextHandleMove` |
| Task complete | `playCompleteSound()` | `HapticFeedbackType.LongPress` |
| Level up | `playLevelUpSound()` | `HapticFeedbackType.LongPress` |

```kotlin
// Haptic usage in composables
val haptic = LocalHapticFeedback.current
haptic.performHapticFeedback(HapticFeedbackType.LongPress)
```

---

## SoundPool Limits

`SoundPool` is initialized with `maxStreams = 4`. Simultaneous sounds beyond 4 will be dropped. Current triggers are designed to not overlap (tap is always short; level-up only fires once per session interaction).
