package com.example.myapplication.ui.components

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.compose.OnParticleSystemUpdateListener
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.Spread
import nl.dionsegijn.konfetti.core.emitter.Emitter
import nl.dionsegijn.konfetti.core.models.Shape
import nl.dionsegijn.konfetti.core.models.Size
import java.util.concurrent.TimeUnit

/**
 * Konfetti confetti burst — fires from the top-center of the screen.
 * Replaces the old custom Canvas implementation.
 */
@Composable
fun ConfettiAnimation(
    modifier: Modifier = Modifier,
    onAnimationEnd: () -> Unit = {}
) {
    val parties = remember {
        listOf(
            Party(
                speed       = 0f,
                maxSpeed    = 30f,
                damping     = 0.9f,
                angle       = 270,
                spread      = Spread.ROUND,
                colors      = listOf(
                    0xFFEF5350.toInt(), // red
                    0xFF42A5F5.toInt(), // blue
                    0xFFABFF4F.toInt(), // green
                    0xFFFFEE58.toInt(), // yellow
                    0xFFAB47BC.toInt(), // purple
                    0xFF26C6DA.toInt(), // cyan
                    0xFFF59E0B.toInt(), // amber
                    0xFFFF7043.toInt()  // deep orange
                ),
                shapes      = listOf(Shape.Square, Shape.Circle),
                size        = listOf(Size.SMALL, Size.MEDIUM),
                timeToLive  = 2000L,
                position    = Position.Relative(0.5, 0.0),
                emitter     = Emitter(duration = 100, TimeUnit.MILLISECONDS).max(150)
            )
        )
    }

    KonfettiView(
        modifier = modifier.fillMaxSize(),
        parties  = parties,
        updateListener = object : OnParticleSystemUpdateListener {
            override fun onParticleSystemEnded(system: nl.dionsegijn.konfetti.core.PartySystem, activeSystems: Int) {
                if (activeSystems == 0) onAnimationEnd()
            }
        }
    )
}
