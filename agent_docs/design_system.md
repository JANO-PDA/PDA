# Design System — PDA App

Full reference for colors, shapes, typography, and glassmorphism specs. Always read this before creating or modifying UI components.

---

## Glassmorphism Spec

Cards and surfaces use semi-transparent fills — never opaque:

```kotlin
// Card surface (most common)
surface.copy(alpha = 0.85f)

// Elevated card / modal sheet
surface.copy(alpha = 0.92f)

// Overlay / scrim layer
surface.copy(alpha = 0.97f)
```

Combine with a subtle border:
```kotlin
Border: MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), 1.dp
```

---

## Color Tokens

Defined in `app/src/main/java/com/example/myapplication/ui/theme/Color.kt`.

### Core Vault Palette

| Token | Hex | Use |
|---|---|---|
| `VaultBackground` | `#0A0A0F` | Root background |
| `VaultSurface` | `#14141C` | Card / sheet background |
| `VaultSurfaceVar` | `#1C1C28` | Elevated surface (dialogs) |
| `VaultSurfaceHigh` | `#242436` | Highest elevation surface |

### Brand / Accent

| Token | Hex | Use |
|---|---|---|
| `AmberGlow` | `#F59E0B` | Primary — XP bars, primary actions, vault terminal amber |
| `AmberDim` | `#B45309` | Pressed/dimmed amber state |
| `TealAccent` | `#14B8A6` | Secondary — scanner lines, shimmer accents |
| `IndigoHighlight` | `#6366F1` | Tertiary — rank progress bars, highlights |
| `ErrorRed` | `#EF4444` | Error states, streak-at-risk flash, overdue indicators |

### Glass Helpers

| Token | Use |
|---|---|
| `GlassWhite5` | `Color.White.copy(alpha = 0.05f)` — subtle shimmer layer |
| `GlassBlack40` | `Color.Black.copy(alpha = 0.40f)` — scrim overlay |

### Category Colors

| Token | Hex | Category |
|---|---|---|
| `CategoryWork` | `#3B82F6` | Work (blue) |
| `CategoryStudy` | `#22C55E` | Study (green) |
| `CategoryHealth` | `#EF4444` | Health (red) |
| `CategoryPersonal` | `#A855F7` | Personal (purple) |
| `CategoryShopping` | `#F97316` | Shopping (orange) |
| `CategoryOther` | `#94A3B8` | Other (slate) |

Use `AppIcons.getCategoryIcon(category)` for matching icons.

### Difficulty Colors

| Token | Hex | Difficulty |
|---|---|---|
| `DiffEasy` | `#4ADE80` | EASY (10 XP) |
| `DiffMedium` | `#FBBF24` | MEDIUM (25 XP) |
| `DiffHard` | `#F97316` | HARD (50 XP) |
| `DiffNightmare` | `#EF4444` | NIGHTMARE (100 XP) |

### Light Theme Fallback

| Token | Use |
|---|---|
| `ParchmentBg` | Root background in light mode |
| `ParchmentSurface` | Card surface in light mode |
| `OnParchment` | Text on parchment surfaces |

---

## Shape Tokens

Defined in `app/src/main/java/com/example/myapplication/ui/theme/Shape.kt`.

> **Note**: Actual code values below. CLAUDE.md lists design-intent values (16dp/24dp) which may differ.

| Token | Actual Value | Use |
|---|---|---|
| `GlassCard` | `RoundedCornerShape(12.dp)` | Task cards, stat cards |
| `GlassPill` | `RoundedCornerShape(50)` | Level badges, streak pill |
| `GlassDialog` | `RoundedCornerShape(20.dp)` | Dialogs, bottom sheets |
| `GlassChip` | `RoundedCornerShape(8.dp)` | Filter chips, small tags |
| `GlassStripe` | `RoundedCornerShape(topStart=2.dp, bottomStart=2.dp)` | Category left-border accent |

---

## Typography

Defined in `app/src/main/java/com/example/myapplication/ui/theme/Type.kt`.

Fonts loaded via Google Fonts provider (`androidx.compose.ui:ui-text-google-fonts`).

| Style Range | Font | Weight | Use |
|---|---|---|---|
| `displayLarge` → `headlineSmall` | Share Tech Mono | Regular | Section headers, XP numbers, vault terminal text |
| `titleLarge` → `titleMedium` | Inter | SemiBold (600) | Card titles, task names |
| `bodyLarge` → `bodyMedium` | Inter | Regular (400) | Descriptions, labels |
| `labelLarge` → `labelSmall` | Inter | Medium (500) | Chips, badges, stat labels |

**Rule**: Always use `MaterialTheme.typography.*` — never hardcode `fontSize` or `fontFamily`.

---

## Spacing Conventions

No formal spacing token system — use these guidelines:

| Context | Value |
|---|---|
| Card internal padding | `16.dp` |
| Screen edge padding | `16.dp` (horizontal), `8.dp` (vertical) |
| Between cards / list items | `8.dp` |
| Between label + value | `4.dp` |
| Icon size (standard) | `20.dp` – `24.dp` |
| Avatar circle | `40.dp` – `48.dp` |

---

## Themes

Three selectable themes, each with light + dark variant. Defined in `ui/theme/Theme.kt`.

| Theme ID | Primary | Secondary | Vibe |
|---|---|---|---|
| `ZONE_EXPLORER` (default) | Amber `#F59E0B` | Teal `#14B8A6` | Classic vault explorer |
| `RADIATION` | Orange-red | Warm orange | Heat / danger zone |
| `PRIPYAT` | Ice blue | Cool grey | Ghost town / cold |

The active theme is stored in `UserProfile.selectedTheme` and persisted via `UserProfileStorage`.

---

## Do / Don't

| Don't | Do |
|---|---|
| Opaque `Color(0xFF14141C)` fills on cards | `surface.copy(alpha = 0.92f)` |
| `AlertDialog` for task creation | `ModalBottomSheet` |
| `FilterChip` grids for difficulty/category | `DifficultyCard` / `CategoryCard` from `AddTaskDialog.kt` |
| `values()` on enums | `.entries` |
| Hardcode font sizes | `MaterialTheme.typography.*` |
| `graphicsLayer { rotationZ = X }` | `Modifier.rotate(X)` |
