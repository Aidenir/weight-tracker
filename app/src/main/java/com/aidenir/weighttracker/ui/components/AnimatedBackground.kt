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

/** Bright accent shades we only use in the background, not in the UI. */
private val WarmOrange = Color(0xFFFFB86B)
private val MintGreen = Color(0xFF6AF0C4)
private val HotCoral = Color(0xFFFF6E9E)

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

private val Blobs = listOf(
    Blob(BrandPrimary, freqX = 0.7f, freqY = 1.1f, phase = 0.00f, amp = 0.35f, cx = 0.35f, cy = 0.30f, radius = 0.85f, alpha = 0.55f),
    Blob(BrandSecondary, freqX = 1.3f, freqY = 0.9f, phase = 0.30f, amp = 0.42f, cx = 0.65f, cy = 0.75f, radius = 0.80f, alpha = 0.42f),
    Blob(BrandAccent, freqX = 0.9f, freqY = 1.4f, phase = 0.60f, amp = 0.34f, cx = 0.55f, cy = 0.50f, radius = 0.68f, alpha = 0.42f),
    Blob(WarmOrange, freqX = 1.1f, freqY = 0.6f, phase = 0.15f, amp = 0.48f, cx = 0.20f, cy = 0.80f, radius = 0.55f, alpha = 0.28f),
    Blob(MintGreen, freqX = 0.5f, freqY = 1.2f, phase = 0.80f, amp = 0.40f, cx = 0.80f, cy = 0.20f, radius = 0.60f, alpha = 0.30f),
    Blob(HotCoral, freqX = 0.8f, freqY = 0.7f, phase = 0.45f, amp = 0.36f, cx = 0.15f, cy = 0.15f, radius = 0.50f, alpha = 0.34f)
)

private const val BUBBLE_COUNT = 22
private const val SPARKLE_COUNT = 40

/** Cheap deterministic hash → 0..1. */
private fun hash01(i: Int, salt: Int): Float {
    val s = (i.toUInt() * 2654435761u + salt.toUInt() * 40503u).toInt()
    return (s and 0x7FFFFFFF) / 2147483647f
}

/**
 * Animated aurora-style playground: base gradient + wandering color blobs +
 * bubbles drifting upward + softly pulsing sparkle dots. Drives the liquid
 * glass blur above it — the more variety here, the richer the glass reads.
 */
@Composable
fun AnimatedBackground(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "bg")
    val t by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 30_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bg-t"
    )
    val bubble by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 18_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bg-bubble"
    )
    val sparkle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bg-sparkle"
    )

    val bubbleSeeds = remember {
        List(BUBBLE_COUNT) { i ->
            BubbleSeed(
                lane = hash01(i, 1),
                jitter = hash01(i, 2),
                offset = hash01(i, 3),
                size = hash01(i, 4),
                tint = hash01(i, 5)
            )
        }
    }
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
        drawBubbles(bubble, bubbleSeeds)
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

private data class BubbleSeed(
    val lane: Float,
    val jitter: Float,
    val offset: Float,
    val size: Float,
    val tint: Float
)

private fun DrawScope.drawBubbles(t: Float, seeds: List<BubbleSeed>) {
    val w = size.width
    val h = size.height
    val tints = listOf(
        Color.White,
        BrandSecondary,
        MintGreen,
        BrandAccent
    )

    for (seed in seeds) {
        // vertical progress: 0 = bottom, 1 = top, wraps every cycle
        val progress = (t + seed.offset) % 1f
        val y = h * (1f - progress) - 40f
        // gentle horizontal swaying
        val sway = sin((t * 2 * PI + seed.jitter * 2 * PI).toFloat()) * 30f
        val x = w * seed.lane + sway
        val radius = 4f + seed.size * 18f
        // fade in first 15% then out over last 20%
        val alpha = when {
            progress < 0.15f -> progress / 0.15f
            progress > 0.80f -> (1f - progress) / 0.20f
            else -> 1f
        } * (0.22f + seed.size * 0.08f)
        val color = tints[(seed.tint * tints.size).toInt().coerceIn(0, tints.size - 1)]
        drawCircle(
            color = color.copy(alpha = alpha),
            radius = radius,
            center = Offset(x, y)
        )
        // Inner highlight for a wet-glass feel
        drawCircle(
            color = Color.White.copy(alpha = alpha * 0.6f),
            radius = radius * 0.35f,
            center = Offset(x - radius * 0.35f, y - radius * 0.35f)
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
        val radius = 1.2f + seed.twinkle * 2.2f
        val alpha = brightness * (0.35f + seed.twinkle * 0.35f)
        val cx = seed.x * w
        val cy = seed.y * h
        drawCircle(
            color = Color.White.copy(alpha = alpha),
            radius = radius,
            center = Offset(cx, cy)
        )
        // small radial glow for a subtle halo
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = alpha * 0.35f),
                    Color.Transparent
                ),
                center = Offset(cx, cy),
                radius = radius * 6f
            ),
            radius = radius * 6f,
            center = Offset(cx, cy)
        )
    }
}
