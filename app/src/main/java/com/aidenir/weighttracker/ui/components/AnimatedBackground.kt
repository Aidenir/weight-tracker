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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.aidenir.weighttracker.ui.theme.BgBottom
import com.aidenir.weighttracker.ui.theme.BgMid
import com.aidenir.weighttracker.ui.theme.BgTop
import com.aidenir.weighttracker.ui.theme.BrandAccent
import com.aidenir.weighttracker.ui.theme.BrandPrimary
import com.aidenir.weighttracker.ui.theme.BrandSecondary
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

private data class Blob(
    val color: Color,
    val freqX: Float,
    val freqY: Float,
    val phase: Float,
    val amp: Float,
    val cx: Float,
    val cy: Float,
    val radius: Float,
    val alpha: Float
)

// Three broad glows in the same cool family — enough colour movement for the
// glass to catch, without the disco-lights feel of the previous six-hue mix.
private val Blobs = listOf(
    Blob(BrandPrimary,   freqX = 0.4f, freqY = 0.6f, phase = 0.00f, amp = 0.18f, cx = 0.30f, cy = 0.25f, radius = 0.95f, alpha = 0.22f),
    Blob(BrandAccent,    freqX = 0.5f, freqY = 0.3f, phase = 0.35f, amp = 0.16f, cx = 0.70f, cy = 0.65f, radius = 0.85f, alpha = 0.16f),
    Blob(BrandSecondary, freqX = 0.3f, freqY = 0.5f, phase = 0.70f, amp = 0.20f, cx = 0.55f, cy = 0.90f, radius = 0.70f, alpha = 0.14f)
)

private const val SPARKLE_COUNT = 10

/** Cheap deterministic hash → 0..1. */
private fun hash01(i: Int, salt: Int): Float {
    val s = (i.toUInt() * 2654435761u + salt.toUInt() * 40503u).toInt()
    return (s and 0x7FFFFFFF) / 2147483647f
}

/**
 * A calm, slow-shifting night sky. The blobs are barely-there colour drift
 * that gives the liquid glass something to sample; the sparkles are a handful
 * of faint stars, not a starfield.
 */
@Composable
fun AnimatedBackground(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "bg")
    val t by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 60_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bg-t"
    )
    val sparkle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bg-sparkle"
    )

    val sparkleSeeds = remember {
        List(SPARKLE_COUNT) { i ->
            SparkleSeed(
                x = hash01(i, 11),
                y = hash01(i, 13),
                phase = hash01(i, 17),
                twinkle = hash01(i, 19)
            )
        }
    }

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
        drawBlobs(t)
        drawSparkles(sparkle, sparkleSeeds)
    }
}

private fun DrawScope.drawBlobs(t: Float) {
    val w = size.width
    val h = size.height
    val diag = kotlin.math.hypot(w.toDouble(), h.toDouble()).toFloat()

    for (b in Blobs) {
        val angleX = (t * 2 * PI * b.freqX + b.phase * 2 * PI).toFloat()
        val angleY = (t * 2 * PI * b.freqY + b.phase * 2 * PI * 1.3f).toFloat()
        val center = Offset(
            x = w * (b.cx + b.amp * cos(angleX)),
            y = h * (b.cy + b.amp * sin(angleY))
        )
        val radius = diag * b.radius * 0.55f
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(b.color.copy(alpha = b.alpha), Color.Transparent),
                center = center,
                radius = radius
            ),
            size = Size(w, h)
        )
    }
}

private data class SparkleSeed(
    val x: Float,
    val y: Float,
    val phase: Float,
    val twinkle: Float
)

private fun DrawScope.drawSparkles(t: Float, seeds: List<SparkleSeed>) {
    val w = size.width
    val h = size.height

    for (seed in seeds) {
        val phase = (t + seed.phase) % 1f
        // triangle wave 0→1→0
        val wave = 1f - abs(phase * 2f - 1f)
        val brightness = wave * wave           // sharper falloff
        val radius = 0.8f + seed.twinkle * 1.4f
        val alpha = brightness * (0.14f + seed.twinkle * 0.10f)
        val cx = seed.x * w
        val cy = seed.y * h
        drawCircle(
            color = Color.White.copy(alpha = alpha),
            radius = radius,
            center = Offset(cx, cy)
        )
    }
}
