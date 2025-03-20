package com.example.myapplication.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.airbnb.lottie.compose.*
import com.example.myapplication.R
import kotlinx.coroutines.delay

/**
 * A simplified task creation animation that shows a subtle animation
 * to indicate a task has been created. This is designed to be used inside
 * a task item rather than as a full-screen overlay.
 */
@Composable
fun TaskCreatedAnimation(
    isVisible: Boolean,
    onAnimationFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Don't show anything if not visible
    if (!isVisible) return
    
    // Load the animation
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(
        R.raw.task_created
    ))
    
    // Animation visibility
    val alpha = remember { Animatable(0f) }
    
    // Control animation state
    LaunchedEffect(isVisible) {
        if (isVisible) {
            // Fade in
            alpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(300)
            )
            
            // Keep visible for a while
            delay(1000)
            
            // Fade out
            alpha.animateTo(
                targetValue = 0f,
                animationSpec = tween(500)
            )
            
            // Animation complete
            onAnimationFinished()
        }
    }
    
    // Get the highlight color from the theme
    val highlightColor = androidx.compose.material3.MaterialTheme.colorScheme.primary
    
    // Play the animation once
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = 1,
        isPlaying = isVisible,
        speed = 1.2f
    )
    
    // Render the animation
    LottieAnimation(
        composition = composition,
        progress = { progress },
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
            .padding(vertical = 8.dp)
            .alpha(alpha.value)
    )
} 