package com.aidenir.weighttracker.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkGlassColors = darkColorScheme(
    primary = BrandPrimary,
    onPrimary = Color.White,
    secondary = BrandSecondary,
    onSecondary = Color.Black,
    tertiary = BrandAccent,
    onTertiary = Color.White,
    background = BgTop,
    onBackground = TextPrimary,
    surface = BgMid,
    onSurface = TextPrimary,
    surfaceVariant = Color(0x2AFFFFFF),
    onSurfaceVariant = TextSecondary,
    outline = Color(0x33FFFFFF)
)

@Composable
fun WeightTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? android.app.Activity)?.window
            if (window != null) {
                window.statusBarColor = Color.Transparent.toArgb()
                window.navigationBarColor = Color.Transparent.toArgb()
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = false
                controller.isAppearanceLightNavigationBars = false
                WindowCompat.setDecorFitsSystemWindows(window, false)
            }
        }
    }

    MaterialTheme(
        colorScheme = DarkGlassColors,
        typography = AppTypography,
        content = content
    )
}
