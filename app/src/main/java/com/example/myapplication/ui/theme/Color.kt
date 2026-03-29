package com.example.myapplication.ui.theme

import androidx.compose.ui.graphics.Color

// ─── Vault Dark Palette ────────────────────────────────────────────────────
val VaultBackground    = Color(0xFF0A0A0F)   // near-black with slight blue undertone
val VaultSurface       = Color(0xFF14141C)   // card background
val VaultSurfaceVar    = Color(0xFF1C1C28)   // elevated card / drawer
val VaultSurfaceHigh   = Color(0xFF252535)   // top-level surfaces like TopAppBar

val AmberGlow          = Color(0xFFF59E0B)   // primary — vault terminal amber
val AmberDim           = Color(0xFFB45309)   // darker amber for light theme
val TealAccent         = Color(0xFF14B8A6)   // secondary — teal scanner line
val IndigoHighlight    = Color(0xFF6366F1)   // tertiary — indigo highlight
val ErrorRed           = Color(0xFFEF4444)

val OnVaultDark        = Color(0xFFE8E8F0)   // primary text on dark surfaces
val OnVaultMuted       = Color(0xFF9999B0)   // secondary / muted text

// ─── Vault Light Palette ────────────────────────────────────────────────────
val ParchmentBg        = Color(0xFFF5F0E8)
val ParchmentSurface   = Color(0xFFFFFFFF)
val ParchmentSurfaceVar = Color(0xFFF0EBE0)
val OnParchment        = Color(0xFF1A1A2E)

// ─── Zone Explorer accents (amber + teal — default vault) ───────────────────
val ZoneAmberPrimary   = Color(0xFFF59E0B)
val ZoneTealSecondary  = Color(0xFF14B8A6)

// ─── Radiation accents (orange-red) ─────────────────────────────────────────
val RadiationPrimary   = Color(0xFFF97316)
val RadiationSecondary = Color(0xFFEA580C)
val RadiationWarn      = Color(0xFFFFED4A)

// ─── Pripyat accents (ice blue) ─────────────────────────────────────────────
val PripyatPrimary     = Color(0xFF38BDF8)
val PripyatSecondary   = Color(0xFF0284C7)
val PripyatIce         = Color(0xFFBAE6FD)

// ─── Category accent colors ──────────────────────────────────────────────────
val CategoryWork       = Color(0xFF3B82F6)   // blue
val CategoryStudy      = Color(0xFF22C55E)   // green
val CategoryHealth     = Color(0xFFEF4444)   // red
val CategoryPersonal   = Color(0xFFA855F7)   // purple
val CategoryShopping   = Color(0xFFF97316)   // orange
val CategoryOther      = Color(0xFF94A3B8)   // slate

// ─── Difficulty badge colors ─────────────────────────────────────────────────
val DiffEasy           = Color(0xFF4ADE80)   // green-400
val DiffMedium         = Color(0xFFFBBF24)   // amber-400
val DiffHard           = Color(0xFFF97316)   // orange-500
val DiffNightmare      = Color(0xFFEF4444)   // red-500

// ─── Glass layers ─────────────────────────────────────────────────────────────
val GlassWhite10       = Color(0x1AFFFFFF)   // white 10% — frosted highlight
val GlassWhite5        = Color(0x0DFFFFFF)   // white 5%  — subtle layer
val GlassBlack40       = Color(0x66000000)   // scrim / overlay
