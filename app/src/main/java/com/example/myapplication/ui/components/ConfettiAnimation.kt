package com.example.myapplication.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.sin
import kotlin.random.Random

/**
 * A fun confetti animation to celebrate task completion
 */
@Composable
fun ConfettiAnimation(
    numConfetti: Int = 100,
    durationMillis: Int = 2000,
    onAnimationEnd: () -> Unit = {}
) {
    val density = LocalDensity.current
    
    // Convert dp to pixels for calculations
    val particleSize = with(density) { 8.dp.toPx() }
    
    // Animation state
    var showAnimation by remember { mutableStateOf(true) }
    
    // End animation after duration
    LaunchedEffect(Unit) {
        delay(durationMillis.toLong())
        showAnimation = false
        onAnimationEnd()
    }
    
    if (!showAnimation) return
    
    // Create confetti particles
    val particles = remember {
        List(numConfetti) {
            Particle(
                id = it,
                x = Random.nextFloat() * 100f,
                y = -50f - Random.nextFloat() * 400f,
                radius = particleSize * (0.5f + Random.nextFloat() * 0.5f),
                velocity = Random.nextFloat() * 3f + 2f,
                angle = Random.nextFloat() * 360f,
                rotationSpeed = Random.nextFloat() * 800f - 400f,
                color = listOf(
                    Color(0xFFEF5350), // Red
                    Color(0xFF42A5F5), // Blue
                    Color(0xFFABFF4F), // Green
                    Color(0xFFFFEE58), // Yellow
                    Color(0xFFAB47BC), // Purple
                    Color(0xFF26C6DA), // Cyan
                    Color(0xFFFFCA28), // Amber
                    Color(0xFFFF7043)  // Deep Orange
                ).random()
            )
        }
    }
    
    // Animation progress
    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = durationMillis,
                easing = LinearEasing
            )
        )
    }
    
    // Draw the confetti
    Canvas(modifier = Modifier.fillMaxSize()) {
        particles.forEach { particle ->
            // Calculate position based on animation progress
            val progress = animationProgress.value
            
            // Start position off screen (top)
            val startX = particle.x / 100f * size.width
            val startY = particle.y
            
            // End position (bottom of screen)
            val endY = size.height + particle.radius
            
            // Current position with gravity effect
            val currentX = startX + sin(particle.angle * Math.PI.toFloat() / 180f) * 100 * progress
            val gravityEffect = progress * progress * 2 // Quadratic gravity effect
            val currentY = startY + (endY - startY) * gravityEffect
            
            // Draw the confetti with rotation
            drawConfetti(
                particle = particle,
                x = currentX,
                y = currentY,
                rotation = particle.angle + particle.rotationSpeed * progress
            )
        }
    }
}

// Helper extension function to draw confetti with rotation
private fun DrawScope.drawConfetti(
    particle: Particle,
    x: Float,
    y: Float,
    rotation: Float
) {
    rotate(rotation, Offset(x, y)) {
        // Draw different shapes for variety
        when (particle.id % 3) {
            // Circle
            0 -> drawCircle(
                color = particle.color,
                radius = particle.radius,
                center = Offset(x, y)
            )
            // Square
            1 -> drawRect(
                color = particle.color,
                topLeft = Offset(x - particle.radius, y - particle.radius),
                size = androidx.compose.ui.geometry.Size(
                    particle.radius * 2,
                    particle.radius * 2
                )
            )
            // Rectangle
            else -> drawRect(
                color = particle.color,
                topLeft = Offset(x - particle.radius, y - particle.radius / 2),
                size = androidx.compose.ui.geometry.Size(
                    particle.radius * 2,
                    particle.radius
                )
            )
        }
    }
}

/**
 * Data class representing a confetti particle
 */
private data class Particle(
    val id: Int,
    val x: Float,
    val y: Float,
    val radius: Float,
    val velocity: Float,
    val angle: Float,
    val rotationSpeed: Float,
    val color: Color
) 