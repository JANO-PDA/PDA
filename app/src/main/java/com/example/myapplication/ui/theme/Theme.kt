package com.example.myapplication.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import com.example.myapplication.data.models.AppTheme

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

private val ZoneExplorerLightColors = lightColorScheme(
    primary = Color(0xFF2E7D32),
    secondary = Color(0xFF558B2F),
    tertiary = Color(0xFF81C784),
    background = Color(0xFFF5F5F5),
    surface = Color(0xFFFFFFFF),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.Black,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F)
)

private val ZoneExplorerDarkColors = darkColorScheme(
    primary = Color(0xFF81C784),
    secondary = Color(0xFF558B2F),
    tertiary = Color(0xFF2E7D32),
    background = Color(0xFF1C1B1F),
    surface = Color(0xFF2D2D2D),
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

private val RadiationLightColors = lightColorScheme(
    primary = Color(0xFFE65100),
    secondary = Color(0xFFF57C00),
    tertiary = Color(0xFFFFB74D),
    background = Color(0xFFFFF3E0),
    surface = Color(0xFFFFFFFF),
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F)
)

private val RadiationDarkColors = darkColorScheme(
    primary = Color(0xFFFFB74D),
    secondary = Color(0xFFF57C00),
    tertiary = Color(0xFFE65100),
    background = Color(0xFF1C1B1F),
    surface = Color(0xFF2D2D2D),
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

private val PripyatLightColors = lightColorScheme(
    primary = Color(0xFF1976D2),
    secondary = Color(0xFF2196F3),
    tertiary = Color(0xFF64B5F6),
    background = Color(0xFFE3F2FD),
    surface = Color(0xFFFFFFFF),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.Black,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F)
)

private val PripyatDarkColors = darkColorScheme(
    primary = Color(0xFF64B5F6),
    secondary = Color(0xFF2196F3),
    tertiary = Color(0xFF1976D2),
    background = Color(0xFF1C1B1F),
    surface = Color(0xFF2D2D2D),
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun TodoAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    appTheme: AppTheme = AppTheme.ZONE_EXPLORER,
    content: @Composable () -> Unit
) {
    val colorScheme = when (appTheme) {
        AppTheme.ZONE_EXPLORER -> if (darkTheme) ZoneExplorerDarkColors else ZoneExplorerLightColors
        AppTheme.RADIATION -> if (darkTheme) RadiationDarkColors else RadiationLightColors
        AppTheme.PRIPYAT -> if (darkTheme) PripyatDarkColors else PripyatLightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}