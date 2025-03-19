package com.example.myapplication.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * An icon that continuously animates with pulsating and rotation effects
 */
@Composable
fun PulsatingIcon(
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.primary,
    pulseEnabled: Boolean = true,
    rotateEnabled: Boolean = true,
    pulseMinScale: Float = 0.8f,
    pulseMaxScale: Float = 1.2f,
    pulseDurationMs: Int = 1500,
    rotateDurationMs: Int = 6000,
) {
    // Create infinite transitions for continuous animations
    val infiniteTransition = rememberInfiniteTransition(label = "iconAnimation")
    
    // Pulsating scale animation
    val scale by infiniteTransition.animateFloat(
        initialValue = pulseMinScale,
        targetValue = pulseMaxScale,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = pulseDurationMs, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    // Rotation animation
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = rotateDurationMs, easing = LinearEasing)
        ),
        label = "rotation"
    )
    
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier
                .scale(if (pulseEnabled) scale else 1f)
                .rotate(if (rotateEnabled) rotation else 0f)
        )
    }
} 