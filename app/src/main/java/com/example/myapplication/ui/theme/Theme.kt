package com.example.myapplication.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.myapplication.data.models.AppTheme

// ─── Zone Explorer — amber glow + teal (default vault look) ─────────────────
private val ZoneExplorerDark = darkColorScheme(
    primary            = ZoneAmberPrimary,
    onPrimary          = Color(0xFF1A0F00),
    primaryContainer   = Color(0xFF3D2800),
    onPrimaryContainer = Color(0xFFFFDFA0),
    secondary          = ZoneTealSecondary,
    onSecondary        = Color(0xFF001F1D),
    secondaryContainer = Color(0xFF003733),
    onSecondaryContainer = Color(0xFF70F5E8),
    tertiary           = IndigoHighlight,
    onTertiary         = Color(0xFF0F0F3A),
    tertiaryContainer  = Color(0xFF1E1E5C),
    onTertiaryContainer = Color(0xFFBEBEFF),
    background         = VaultBackground,
    onBackground       = OnVaultDark,
    surface            = VaultSurface,
    onSurface          = OnVaultDark,
    surfaceVariant     = VaultSurfaceVar,
    onSurfaceVariant   = OnVaultMuted,
    surfaceTint        = ZoneAmberPrimary,
    error              = ErrorRed,
    onError            = Color.White,
    errorContainer     = Color(0xFF500000),
    onErrorContainer   = Color(0xFFFFB4A9)
)

private val ZoneExplorerLight = lightColorScheme(
    primary            = AmberDim,
    onPrimary          = Color.White,
    primaryContainer   = Color(0xFFFFE0A0),
    onPrimaryContainer = Color(0xFF2C1600),
    secondary          = Color(0xFF0D9488),
    onSecondary        = Color.White,
    secondaryContainer = Color(0xFFB2F5EF),
    onSecondaryContainer = Color(0xFF00312D),
    tertiary           = Color(0xFF4F46E5),
    onTertiary         = Color.White,
    background         = ParchmentBg,
    onBackground       = OnParchment,
    surface            = ParchmentSurface,
    onSurface          = OnParchment,
    surfaceVariant     = ParchmentSurfaceVar,
    onSurfaceVariant   = Color(0xFF4A4A6A),
    error              = Color(0xFFDC2626),
    onError            = Color.White
)

// ─── Radiation — orange-red (danger / heat) ───────────────────────────────────
private val RadiationDark = darkColorScheme(
    primary            = RadiationPrimary,
    onPrimary          = Color(0xFF1A0800),
    primaryContainer   = Color(0xFF3D1800),
    onPrimaryContainer = Color(0xFFFFD0A0),
    secondary          = RadiationWarn,
    onSecondary        = Color(0xFF1A1400),
    secondaryContainer = Color(0xFF3D3000),
    onSecondaryContainer = Color(0xFFFFF0A0),
    tertiary           = ErrorRed,
    onTertiary         = Color.White,
    background         = Color(0xFF0F0A00),
    onBackground       = Color(0xFFFFEDD5),
    surface            = Color(0xFF1A1000),
    onSurface          = Color(0xFFFFEDD5),
    surfaceVariant     = Color(0xFF241800),
    onSurfaceVariant   = Color(0xFFBBA080),
    error              = ErrorRed,
    onError            = Color.White,
    errorContainer     = Color(0xFF500000),
    onErrorContainer   = Color(0xFFFFB4A9)
)

private val RadiationLight = lightColorScheme(
    primary            = Color(0xFFEA580C),
    onPrimary          = Color.White,
    primaryContainer   = Color(0xFFFFDDC4),
    onPrimaryContainer = Color(0xFF2C0A00),
    secondary          = Color(0xFFB45309),
    onSecondary        = Color.White,
    background         = Color(0xFFFFF7ED),
    onBackground       = Color(0xFF1A0A00),
    surface            = Color.White,
    onSurface          = Color(0xFF1A0A00),
    surfaceVariant     = Color(0xFFFFF0E0),
    onSurfaceVariant   = Color(0xFF6A3A20),
    error              = Color(0xFFDC2626),
    onError            = Color.White
)

// ─── Pripyat — ice blue (cold / ghost city) ──────────────────────────────────
private val PripyatDark = darkColorScheme(
    primary            = PripyatPrimary,
    onPrimary          = Color(0xFF001428),
    primaryContainer   = Color(0xFF003050),
    onPrimaryContainer = Color(0xFFB0E8FF),
    secondary          = PripyatIce,
    onSecondary        = Color(0xFF001428),
    secondaryContainer = Color(0xFF00284A),
    onSecondaryContainer = Color(0xFFD0F4FF),
    tertiary           = TealAccent,
    onTertiary         = Color(0xFF001F1D),
    background         = Color(0xFF030912),
    onBackground       = Color(0xFFD0E8FF),
    surface            = Color(0xFF0A1020),
    onSurface          = Color(0xFFD0E8FF),
    surfaceVariant     = Color(0xFF101828),
    onSurfaceVariant   = Color(0xFF7090B0),
    error              = ErrorRed,
    onError            = Color.White,
    errorContainer     = Color(0xFF500000),
    onErrorContainer   = Color(0xFFFFB4A9)
)

private val PripyatLight = lightColorScheme(
    primary            = PripyatSecondary,
    onPrimary          = Color.White,
    primaryContainer   = Color(0xFFD0EEFF),
    onPrimaryContainer = Color(0xFF001428),
    secondary          = Color(0xFF0369A1),
    onSecondary        = Color.White,
    background         = Color(0xFFEFF8FF),
    onBackground       = Color(0xFF001428),
    surface            = Color.White,
    onSurface          = Color(0xFF001428),
    surfaceVariant     = Color(0xFFE0F0FF),
    onSurfaceVariant   = Color(0xFF20405A),
    error              = Color(0xFFDC2626),
    onError            = Color.White
)

@Composable
fun TodoAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    useDarkTheme: Boolean? = null,
    appTheme: AppTheme = AppTheme.ZONE_EXPLORER,
    content: @Composable () -> Unit
) {
    val isDark = useDarkTheme ?: darkTheme

    val colorScheme = when (appTheme) {
        AppTheme.ZONE_EXPLORER -> if (isDark) ZoneExplorerDark else ZoneExplorerLight
        AppTheme.RADIATION     -> if (isDark) RadiationDark    else RadiationLight
        AppTheme.PRIPYAT       -> if (isDark) PripyatDark      else PripyatLight
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        content     = content
    )
}
