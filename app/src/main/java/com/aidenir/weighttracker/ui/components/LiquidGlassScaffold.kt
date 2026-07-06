package com.aidenir.weighttracker.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop

/**
 * Wraps a screen in the animated background + captures it as a Backdrop layer
 * so any child GlassCard can blur it.
 *
 * Usage:
 * ```
 * LiquidGlassScaffold { backdrop ->
 *     Column { GlassCard(backdrop) { ... } }
 * }
 * ```
 */
@Composable
fun LiquidGlassScaffold(
    modifier: Modifier = Modifier,
    content: @Composable (Backdrop) -> Unit
) {
    val backdrop = rememberLayerBackdrop()
    Box(modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .layerBackdrop(backdrop)
        ) {
            AnimatedBackground()
        }
        content(backdrop)
    }
}
