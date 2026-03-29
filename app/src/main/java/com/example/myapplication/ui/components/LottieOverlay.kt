package com.example.myapplication.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.*
import com.example.myapplication.ui.theme.GlassBlack40

/**
 * Full-screen Lottie overlay — used for level-up fanfare.
 * Plays once then calls [onFinished].
 *
 * Place the Lottie JSON file at: app/src/main/assets/lottie_level_up.json
 */
@Composable
fun LevelUpOverlay(
    visible: Boolean,
    onFinished: () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter   = fadeIn(),
        exit    = fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(GlassBlack40),
            contentAlignment = Alignment.Center
        ) {
            LottieOnce(
                assetName  = "lottie_level_up.json",
                size       = 280.dp,
                onFinished = onFinished
            )
        }
    }
}

/**
 * Small inline Lottie that plays once (e.g., on task complete).
 * Shows nothing if the asset file is missing.
 *
 * Place the Lottie JSON file at: app/src/main/assets/lottie_task_complete.json
 */
@Composable
fun TaskCompleteLottie(
    visible: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 80.dp,
    onFinished: () -> Unit = {}
) {
    AnimatedVisibility(
        visible = visible,
        enter   = fadeIn(),
        exit    = fadeOut()
    ) {
        LottieOnce(assetName = "lottie_task_complete.json", size = size, modifier = modifier, onFinished = onFinished)
    }
}

// ─── Internal helper ─────────────────────────────────────────────────────────

@Composable
private fun LottieOnce(
    assetName: String,
    size: Dp,
    modifier: Modifier = Modifier,
    onFinished: () -> Unit = {}
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.Asset(assetName))
    val progress    by animateLottieCompositionAsState(
        composition = composition,
        iterations  = 1,
        isPlaying   = composition != null
    )

    LaunchedEffect(progress) {
        if (progress >= 1f && composition != null) onFinished()
    }

    if (composition != null) {
        com.airbnb.lottie.compose.LottieAnimation(
            composition = composition,
            progress    = { progress },
            modifier    = modifier.size(size)
        )
    }
}

/**
 * Empty-state Lottie that loops indefinitely.
 * Place the file at: app/src/main/assets/lottie_empty.json
 */
@Composable
fun EmptyStateLottie(modifier: Modifier = Modifier, size: Dp = 160.dp) {
    val composition by rememberLottieComposition(LottieCompositionSpec.Asset("lottie_empty.json"))
    val progress    by animateLottieCompositionAsState(
        composition = composition,
        iterations  = LottieConstants.IterateForever
    )
    if (composition != null) {
        com.airbnb.lottie.compose.LottieAnimation(
            composition = composition,
            progress    = { progress },
            modifier    = modifier.size(size)
        )
    }
}
