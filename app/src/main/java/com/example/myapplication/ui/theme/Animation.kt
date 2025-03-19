package com.example.myapplication.ui.theme

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.IntSize

// Re-export common animation specs for consistency
val FastOutSlowInEasing = androidx.compose.animation.core.FastOutSlowInEasing

// Text style constants
val lineThrough = TextDecoration.LineThrough
val none = TextDecoration.None

/**
 * Reusable animation specifications for consistent animations across the app
 */
object AnimationSpecs {
    /**
     * A spring animation with a medium bounce effect
     */
    val SpringBouncy: SpringSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )
    
    /**
     * A spring animation with a low bounce effect
     */
    val SpringLowBouncy: SpringSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessLow
    )
    
    /**
     * A spring animation with no bounce
     */
    val SpringNoBounce: SpringSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )
    
    /**
     * A fast tween animation (300ms)
     */
    val TweenFast: TweenSpec<Float> = tween(
        durationMillis = 300,
        easing = FastOutSlowInEasing
    )
    
    /**
     * A medium tween animation (500ms)
     */
    val TweenMedium: TweenSpec<Float> = tween(
        durationMillis = 500,
        easing = FastOutSlowInEasing
    )
    
    /**
     * A slow tween animation (800ms)
     */
    val TweenSlow: TweenSpec<Float> = tween(
        durationMillis = 800,
        easing = FastOutSlowInEasing
    )
    
    // Pulse animation
    val PulseAnimation = infiniteRepeatable<Float>(
        animation = tween<Float>(800, easing = FastOutSlowInEasing),
        repeatMode = RepeatMode.Reverse
    )
    
    // Screen transitions
    val FadeEnter = fadeIn(
        animationSpec = tween(durationMillis = 500)
    )
    
    val FadeExit = fadeOut(
        animationSpec = tween(durationMillis = 500)
    )
    
    val SlideInHorizontally = slideInHorizontally(
        animationSpec = tween(durationMillis = 500),
        initialOffsetX = { fullWidth -> fullWidth }
    )
    
    val SlideOutHorizontally = slideOutHorizontally(
        animationSpec = tween(durationMillis = 500),
        targetOffsetX = { fullWidth -> -fullWidth }
    )
}

// Extension functions for convenience
@Composable
fun rememberBounceAnimationSpec(): SpringSpec<Float> = spring(
    dampingRatio = Spring.DampingRatioMediumBouncy,
    stiffness = Spring.StiffnessMediumLow
)

// Shake animation values
fun calculateShakeTranslation(time: Long): Float {
    val frequency = 20f // Higher frequency = faster shaking
    val amplitude = 5f // Higher amplitude = bigger shaking
    return (amplitude * kotlin.math.sin(frequency * time / 1000f)).toFloat()
}

// Composable SizeTransform
fun sizeTransformWithIt(enterExit: Pair<EnterTransition, ExitTransition>, clip: Boolean = true): ContentTransform {
    return ContentTransform(
        targetContentEnter = enterExit.first,
        initialContentExit = enterExit.second,
        sizeTransform = SizeTransform(clip = clip)
    )
} 