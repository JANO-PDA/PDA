package com.example.myapplication.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// Custom easing curve
private val EaseInOutCubic = CubicBezierEasing(0.645f, 0.045f, 0.355f, 1.0f)

/**
 * A container that makes its content float continuously up and down
 */
@Composable
fun FloatingElement(
    modifier: Modifier = Modifier,
    floatHeight: Float = 15f, // Maximum floating height in dp
    floatDuration: Int = 2000, // Time for one complete up and down cycle in ms
    content: @Composable () -> Unit
) {
    // Create infinite transitions for continuous animations
    val infiniteTransition = rememberInfiniteTransition(label = "floatingAnimation")
    
    // Floating animation
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = floatHeight,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = floatDuration / 2,
                easing = EaseInOutCubic
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatingOffset"
    )
    
    Box(
        modifier = modifier.offset(y = (-offsetY).dp)
    ) {
        content()
    }
} 