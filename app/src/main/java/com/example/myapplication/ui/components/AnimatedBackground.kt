package com.example.myapplication.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * An animated gradient background that continuously shifts colors
 */
@Composable
fun AnimatedBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "backgroundAnimation")
    
    // Animate the primary color intensity
    val primaryColorRatio by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "primaryColorRatio"
    )
    
    // Animate the secondary color intensity
    val secondaryColorRatio by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 7000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "secondaryColorRatio"
    )
    
    // Animate the gradient angle
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 30000, easing = LinearEasing)
        ),
        label = "gradientAngle"
    )
    
    // Get colors from theme and create animated variants
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.tertiary
    val backgroundColor = MaterialTheme.colorScheme.background
    
    val animatedPrimary = primaryColor.copy(alpha = 0.1f * primaryColorRatio)
    val animatedSecondary = secondaryColor.copy(alpha = 0.1f * secondaryColorRatio)
    
    // Calculate gradient start and end points based on angle
    val angleInRadians = Math.toRadians(angle.toDouble())
    val gradientX = kotlin.math.cos(angleInRadians).toFloat()
    val gradientY = kotlin.math.sin(angleInRadians).toFloat()
    
    // Create animated gradient brush
    val gradientBrush = Brush.linearGradient(
        colors = listOf(backgroundColor, animatedPrimary, animatedSecondary, backgroundColor),
        start = androidx.compose.ui.geometry.Offset(0.5f - gradientX/2, 0.5f - gradientY/2),
        end = androidx.compose.ui.geometry.Offset(0.5f + gradientX/2, 0.5f + gradientY/2)
    )
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(gradientBrush)
    ) {
        content()
    }
} 