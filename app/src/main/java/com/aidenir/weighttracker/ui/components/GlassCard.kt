package com.aidenir.weighttracker.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy

/**
 * A rounded liquid-glass surface that samples [backdrop] and blurs it.
 *
 * Place at least one composable higher in the tree with [Modifier.layerBackdrop]
 * (or use [LiquidGlassScaffold] which does it for you). Content inside is drawn
 * on top of the blurred surface with a subtle tint for legibility.
 */
@Composable
fun GlassCard(
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(22.dp),
    tint: Color = Color.White.copy(alpha = 0.06f),
    blurRadius: Dp = 20.dp,
    lensRadius: Dp = 16.dp,
    lensEdge: Dp = 28.dp,
    contentPadding: Dp = 20.dp,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier.drawBackdrop(
            backdrop = backdrop,
            shape = { shape },
            effects = {
                vibrancy()
                blur(blurRadius.toPx())
                lens(lensRadius.toPx(), lensEdge.toPx())
            },
            onDrawSurface = { drawRect(tint) }
        )
    ) {
        Box(Modifier.padding(contentPadding)) {
            content()
        }
    }
}

/** Same as [GlassCard] but without content padding — for chart / hero surfaces. */
@Composable
fun GlassSurface(
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(22.dp),
    tint: Color = Color.White.copy(alpha = 0.06f),
    blurRadius: Dp = 20.dp,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier.drawBackdrop(
            backdrop = backdrop,
            shape = { shape },
            effects = {
                vibrancy()
                blur(blurRadius.toPx())
                lens(16.dp.toPx(), 28.dp.toPx())
            },
            onDrawSurface = { drawRect(tint) }
        )
    ) {
        content()
    }
}

@Composable
fun rememberScreenBackdrop() = rememberLayerBackdrop()
