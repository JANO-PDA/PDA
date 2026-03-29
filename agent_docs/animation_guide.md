# Animation Guide — PDA App

Which library to use for which animation type, with code patterns from the existing codebase.

---

## Library Decision Tree

```
Is it a celebration / achievement burst?
  └─ YES → Konfetti (ConfettiAnimation.kt)

Is it a complex multi-frame illustration (level-up fanfare, empty state, task complete flash)?
  └─ YES → Lottie (LottieOverlay.kt + assets/*.json)

Is it an ambient/continuous background effect (pulse, shimmer, scan line)?
  └─ YES → InfiniteTransition (Compose built-in)

Is it a response to user interaction (tap, swipe, expand, select)?
  └─ YES → animateXAsState with spring() spec

Is it a data value changing (XP bar fill, count, progress)?
  └─ YES → animateXAsState with tween() spec

Is it screen/element entry or exit?
  └─ YES → AnimatedVisibility with fadeIn + slideInVertically
```

---

## Reusable Specs — `ui/theme/Animation.kt`

Always import from here instead of inline-defining specs.

```kotlin
import com.example.myapplication.ui.theme.*

// Spring specs
SpringBouncy       // DampingRatioMediumBouncy + StiffnessMedium — tap/select
SpringLowBouncy    // DampingRatioLowBouncy + StiffnessLow — expand/collapse
SpringNoBounce     // DampingRatioNoBouncy + StiffnessMedium — slide transitions

// Tween specs
TweenFast          // 300ms FastOutSlowInEasing — quick state changes
TweenMedium        // 500ms FastOutSlowInEasing — medium transitions
TweenSlow          // 800ms FastOutSlowInEasing — deliberate/dramatic changes

// Infinite pulse
PulseAnimation     // InfiniteRepeatableSpec for scale/alpha pulses
```

---

## Pattern 1: Compose Built-in — Micro-Interactions

Use for: tap scale, selection color, swipe offset, expand arrow rotation.

```kotlin
// Scale on interaction
val scale by animateFloatAsState(
    targetValue = if (isSelected) 1.05f else 1f,
    animationSpec = SpringBouncy,
    label = "card_scale"
)

// Color transition
val bgColor by animateColorAsState(
    targetValue = if (isSelected) selectedColor else defaultColor,
    animationSpec = TweenFast,
    label = "card_bg"
)

Modifier.scale(scale).background(bgColor)
```

**Rule**: Never use `animate*AsState` inside a loop body — declare one per animated property at the top of the composable.

---

## Pattern 2: InfiniteTransition — Ambient Effects

Use for: XP bar shimmer, CRT scan line, pulsing icons, badge glow.

```kotlin
val infiniteTransition = rememberInfiniteTransition(label = "ambient")

// Shimmer alpha (6s breathe)
val glowAlpha by infiniteTransition.animateFloat(
    initialValue = 0.06f,
    targetValue = 0.14f,
    animationSpec = infiniteRepeatable(
        animation = tween(6000, easing = FastOutSlowInEasing),
        repeatMode = RepeatMode.Reverse
    ),
    label = "glow_alpha"
)

// Scan line position (2.2s sweep)
val scanOffset by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
        animation = tween(2200, easing = LinearEasing)
    ),
    label = "scan_line"
)
```

**Rule**: Use `InfiniteTransition` — never a `while(true)` coroutine loop for ambient effects.

---

## Pattern 3: Staggered Entry

Use for: dialog fields, list items, screen sections appearing sequentially.

```kotlin
var visible1 by remember { mutableStateOf(false) }
var visible2 by remember { mutableStateOf(false) }
var visible3 by remember { mutableStateOf(false) }

LaunchedEffect(Unit) {
    delay(80);  visible1 = true
    delay(60);  visible2 = true
    delay(60);  visible3 = true
    // total ~700ms for full reveal
}

// Each section:
AnimatedVisibility(
    visible = visible1,
    enter = fadeIn(TweenFast) + slideInVertically { it / 2 }
) { /* content */ }
```

---

## Pattern 4: AnimatedVisibility — Entry/Exit

Use for: overlays, conditional sections, level-up screen.

```kotlin
AnimatedVisibility(
    visible = showOverlay,
    enter = fadeIn(TweenMedium),
    exit = fadeOut(TweenMedium)
) {
    LevelUpOverlay(onDismiss = { showOverlay = false })
}
```

---

## Lottie — `ui/components/LottieOverlay.kt`

Three pre-built composables. Assets in `app/src/main/assets/`.

| Composable | Asset | Trigger | Mode |
|---|---|---|---|
| `LevelUpOverlay` | `lottie_level_up.json` | `viewModel.showLevelUp` state | One-shot, full-screen |
| `TaskCompleteLottie` | `lottie_task_complete.json` | Complete button click | One-shot, inline on card |
| `EmptyStateLottie` | `lottie_empty.json` | No tasks in list | Infinite loop |

```kotlin
// Adding a new Lottie composable
val composition by rememberLottieComposition(
    LottieCompositionSpec.Asset("lottie_my_animation.json")
)
val progress by animateLottieCompositionAsState(
    composition = composition,
    iterations = 1  // or LottieConstants.IterateForever
)
LottieAnimation(composition = composition, progress = { progress })
```

---

## Konfetti — `ui/components/ConfettiAnimation.kt`

Particle burst for task completion. Already configured — call it via ViewModel state.

```kotlin
// In ViewModel — already wired
_showConfetti.value = true

// In UI — already implemented in MainScreen.kt
if (showConfetti) {
    ConfettiAnimation(onFinished = { viewModel.dismissConfetti() })
}
```

Config: 8 colors, ROUND particles, SMALL+MEDIUM sizes, 2s lifetime, 150 max particles.
Do not re-implement — use the existing `ConfettiAnimation` composable.

---

## Duration Guidelines

| Type | Duration | Spec |
|---|---|---|
| Micro-interaction (tap, select) | < 300ms | `TweenFast` or `SpringBouncy` |
| Expand / collapse | 160ms | `tween(160)` |
| Screen transition | 300–500ms | `TweenFast` / `TweenMedium` |
| Data fill (XP bar) | 1200ms | `tween(1200, FastOutSlowInEasing)` |
| Dramatic / celebratory | 800ms–2s | `TweenSlow` or Lottie |
| Ambient pulse cycle | 700ms–6s | `InfiniteTransition` |

---

## Rules

1. `spring()` for interactions — `DampingRatioMediumBouncy` + `StiffnessMedium`
2. `tween(1200, FastOutSlowInEasing)` for data transitions (XP fill, count-up)
3. `InfiniteTransition` for ambient — never a coroutine loop
4. `AnimatedVisibility(fadeIn + slideInVertically)` for sheets; `fadeIn + fadeOut` for overlays
5. One `animate*AsState` per property — never inside a loop
6. Always add a `label` string to every `animate*` call (required for tooling)
