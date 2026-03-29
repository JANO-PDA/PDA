package com.example.myapplication.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.models.TaskCategory
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun VaultEmptyState(
    selectedCategory: TaskCategory?,
    onAddTask: () -> Unit
) {
    val primary   = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val onSurface = MaterialTheme.colorScheme.onSurface

    val infiniteTransition = rememberInfiniteTransition(label = "emptyState")

    // Outer ring rotation — full 360° every 12 seconds
    val outerAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue  = 360f,
        animationSpec = infiniteRepeatable(
            animation  = tween(12_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "outerRing"
    )

    // Inner spokes counter-rotation at half speed
    val innerAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue  = -360f,
        animationSpec = infiniteRepeatable(
            animation  = tween(24_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "innerSpokes"
    )

    // Amber center dot pulse — 0.8→1.2 scale
    val dotScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue  = 1.2f,
        animationSpec = infiniteRepeatable(
            animation  = tween(1_500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dotPulse"
    )

    // Tumbleweed particles — 3 circles floating left → right at different speeds
    val tumbleweed0 by infiniteTransition.animateFloat(
        initialValue = -0.15f,
        targetValue  = 1.15f,
        animationSpec = infiniteRepeatable(
            animation  = tween(4_200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "tw0"
    )
    val tumbleweed1 by infiniteTransition.animateFloat(
        initialValue = -0.15f,
        targetValue  = 1.15f,
        animationSpec = infiniteRepeatable(
            animation  = tween(6_100, easing = LinearEasing, delayMillis = 1_400),
            repeatMode = RepeatMode.Restart
        ),
        label = "tw1"
    )
    val tumbleweed2 by infiniteTransition.animateFloat(
        initialValue = -0.15f,
        targetValue  = 1.15f,
        animationSpec = infiniteRepeatable(
            animation  = tween(5_000, easing = LinearEasing, delayMillis = 2_800),
            repeatMode = RepeatMode.Restart
        ),
        label = "tw2"
    )

    Column(
        modifier            = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Vault door canvas
        Canvas(
            modifier = Modifier.size(180.dp)
        ) {
            val cx      = size.width / 2f
            val cy      = size.height / 2f
            val outerR  = size.minDimension / 2f * 0.9f
            val innerR  = outerR * 0.55f
            val dotR    = outerR * 0.1f * dotScale

            drawVaultDoor(
                cx         = cx,
                cy         = cy,
                outerR     = outerR,
                innerR     = innerR,
                dotR       = dotR,
                outerAngle = outerAngle,
                innerAngle = innerAngle,
                primary    = primary,
                secondary  = secondary
            )

            // Tumbleweed particles
            val twYOffsets = listOf(0.72f, 0.80f, 0.76f)
            val twSizes    = listOf(6f, 4f, 5f)
            listOf(tumbleweed0, tumbleweed1, tumbleweed2).forEachIndexed { idx, progress ->
                drawCircle(
                    color  = primary.copy(alpha = 0.35f),
                    radius = twSizes[idx],
                    center = Offset(progress * size.width, twYOffsets[idx] * size.height)
                )
            }
        }

        // Message block
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text       = "The wasteland is quiet...",
                style      = MaterialTheme.typography.headlineSmall,
                color      = onSurface.copy(alpha = 0.85f),
                textAlign  = TextAlign.Center,
                fontWeight = FontWeight.Normal
            )
            Text(
                text      = "for now",
                style     = MaterialTheme.typography.headlineSmall,
                color     = primary,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text      = if (selectedCategory != null)
                    "No ${selectedCategory.name.lowercase()} missions active"
                else
                    "No active missions",
                style     = MaterialTheme.typography.bodyMedium,
                color     = onSurface.copy(alpha = 0.45f),
                textAlign = TextAlign.Center
            )
        }

        // CTA button
        Button(
            onClick  = onAddTask,
            modifier = Modifier.fillMaxWidth(0.75f)
        ) {
            Text("Add your first mission")
        }
    }
}

private fun DrawScope.drawVaultDoor(
    cx: Float,
    cy: Float,
    outerR: Float,
    innerR: Float,
    dotR: Float,
    outerAngle: Float,
    innerAngle: Float,
    primary: Color,
    secondary: Color
) {
    // Outer rotating ring
    rotate(degrees = outerAngle, pivot = Offset(cx, cy)) {
        // Solid ring arc stroke
        drawCircle(
            color  = primary.copy(alpha = 0.6f),
            radius = outerR,
            center = Offset(cx, cy),
            style  = androidx.compose.ui.graphics.drawscope.Stroke(
                width = 4f,
                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                    intervals = floatArrayOf(28f, 12f),
                    phase     = 0f
                )
            )
        )
        // Tick marks on outer ring
        for (i in 0 until 12) {
            val angle     = Math.toRadians((i * 30).toDouble())
            val startX    = cx + (outerR - 10f) * cos(angle).toFloat()
            val startY    = cy + (outerR - 10f) * sin(angle).toFloat()
            val endX      = cx + outerR * cos(angle).toFloat()
            val endY      = cy + outerR * sin(angle).toFloat()
            drawLine(
                color       = primary.copy(alpha = 0.55f),
                start       = Offset(startX, startY),
                end         = Offset(endX, endY),
                strokeWidth = 2.5f,
                cap         = StrokeCap.Round
            )
        }
    }

    // Mid ring (static subtle)
    drawCircle(
        color  = primary.copy(alpha = 0.12f),
        radius = (outerR + innerR) / 2f,
        center = Offset(cx, cy),
        style  = androidx.compose.ui.graphics.drawscope.Stroke(width = 1f)
    )

    // Inner counter-rotating spokes
    rotate(degrees = innerAngle, pivot = Offset(cx, cy)) {
        for (i in 0 until 8) {
            val angle  = Math.toRadians((i * 45).toDouble())
            val innerX = cx + (innerR * 0.2f) * cos(angle).toFloat()
            val innerY = cy + (innerR * 0.2f) * sin(angle).toFloat()
            val outerX = cx + innerR * cos(angle).toFloat()
            val outerY = cy + innerR * sin(angle).toFloat()
            drawLine(
                color       = secondary.copy(alpha = 0.45f),
                start       = Offset(innerX, innerY),
                end         = Offset(outerX, outerY),
                strokeWidth = 2f,
                cap         = StrokeCap.Round
            )
        }
        // Inner ring
        drawCircle(
            color  = secondary.copy(alpha = 0.3f),
            radius = innerR,
            center = Offset(cx, cy),
            style  = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
        )
    }

    // Center amber pulse dot
    drawCircle(
        color  = primary.copy(alpha = 0.9f),
        radius = dotR,
        center = Offset(cx, cy)
    )
    // Glow halo around dot
    drawCircle(
        color  = primary.copy(alpha = 0.18f),
        radius = dotR * 2.2f,
        center = Offset(cx, cy)
    )
}
