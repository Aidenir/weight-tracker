package com.aidenir.weighttracker.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.aidenir.weighttracker.ui.theme.BgBottom
import com.aidenir.weighttracker.ui.theme.BgMid
import com.aidenir.weighttracker.ui.theme.BgTop
import com.aidenir.weighttracker.ui.theme.BrandAccent
import com.aidenir.weighttracker.ui.theme.BrandPrimary
import com.aidenir.weighttracker.ui.theme.BrandSecondary
import kotlin.math.cos
import kotlin.math.sin

/**
 * Animated aurora-style gradient background. Drives the liquid-glass look above it.
 */
@Composable
fun AnimatedBackground(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "bg")
    val t by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 24_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bg-t"
    )

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    0f to BgTop,
                    0.55f to BgMid,
                    1f to BgBottom
                )
            )
    ) {
        val w = size.width
        val h = size.height
        val angle1 = t * 2 * Math.PI
        val angle2 = (t + 0.33f) * 2 * Math.PI
        val angle3 = (t + 0.66f) * 2 * Math.PI

        drawBlob(
            center = Offset(
                x = w * (0.5f + 0.35f * cos(angle1).toFloat()),
                y = h * (0.30f + 0.20f * sin(angle1).toFloat())
            ),
            radius = w * 0.75f,
            color = BrandPrimary.copy(alpha = 0.55f)
        )
        drawBlob(
            center = Offset(
                x = w * (0.5f + 0.40f * cos(angle2).toFloat()),
                y = h * (0.70f + 0.15f * sin(angle2).toFloat())
            ),
            radius = w * 0.80f,
            color = BrandSecondary.copy(alpha = 0.40f)
        )
        drawBlob(
            center = Offset(
                x = w * (0.5f + 0.30f * cos(angle3).toFloat()),
                y = h * (0.50f + 0.25f * sin(angle3).toFloat())
            ),
            radius = w * 0.65f,
            color = BrandAccent.copy(alpha = 0.35f)
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawBlob(
    center: Offset,
    radius: Float,
    color: Color
) {
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(color, Color.Transparent),
            center = center,
            radius = radius
        ),
        size = Size(size.width, size.height)
    )
}
