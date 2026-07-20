package com.aidenir.weighttracker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.aidenir.weighttracker.ui.theme.BgBottom
import com.aidenir.weighttracker.ui.theme.BgMid
import com.aidenir.weighttracker.ui.theme.BgTop
import com.aidenir.weighttracker.ui.theme.BrandAccent
import com.aidenir.weighttracker.ui.theme.BrandPrimary
import kotlin.math.hypot

/**
 * Static mesh: deep vertical base + two off-centre soft glows.
 * No motion, no sparkle. The Kyant liquid-glass lens effect on cards
 * already creates all the movement the surface needs when you scroll.
 */
@Composable
fun AnimatedBackground(modifier: Modifier = Modifier) {
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    0f to BgTop,
                    0.6f to BgMid,
                    1f to BgBottom
                )
            )
    ) {
        val w = size.width
        val h = size.height
        val diag = hypot(w.toDouble(), h.toDouble()).toFloat()

        // Upper-left periwinkle wash — anchors the top and gives the
        // glass a cool tint to sample.
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    BrandPrimary.copy(alpha = 0.18f),
                    BrandPrimary.copy(alpha = 0.04f),
                    Color.Transparent
                ),
                center = Offset(w * 0.15f, h * 0.10f),
                radius = diag * 0.70f
            )
        )

        // Lower-right muted plum — subtle depth in the opposite corner
        // so the sky doesn't read as flat.
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    BrandAccent.copy(alpha = 0.11f),
                    Color.Transparent
                ),
                center = Offset(w * 0.88f, h * 0.92f),
                radius = diag * 0.55f
            )
        )
    }
}
