package com.aidenir.weighttracker.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.aidenir.weighttracker.ui.theme.DownGood
import com.aidenir.weighttracker.ui.theme.TextMuted
import com.aidenir.weighttracker.ui.theme.TextPrimary
import com.aidenir.weighttracker.ui.theme.UpBad
import com.kyant.backdrop.Backdrop

enum class Trend { UP, DOWN, NEUTRAL }

@Composable
fun MetricPill(
    backdrop: Backdrop,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    trend: Trend = Trend.NEUTRAL
) {
    GlassCard(
        backdrop = backdrop,
        modifier = modifier,
        blurRadius = 20.dp,
        contentPadding = 16.dp
    ) {
        Column {
            Text(
                text = label.uppercase(),
                color = TextMuted,
                style = MaterialTheme.typography.labelMedium
            )
            Spacer(Modifier.height(4.dp))
            val color = when (trend) {
                Trend.UP -> UpBad
                Trend.DOWN -> DownGood
                Trend.NEUTRAL -> TextPrimary
            }
            Text(
                text = value,
                color = color,
                style = MaterialTheme.typography.headlineMedium
            )
        }
    }
}

fun trendFor(deltaKg: Double?): Trend = when {
    deltaKg == null -> Trend.NEUTRAL
    deltaKg > 0.05 -> Trend.UP
    deltaKg < -0.05 -> Trend.DOWN
    else -> Trend.NEUTRAL
}

@Composable
fun ValueColor(trend: Trend): Color = when (trend) {
    Trend.UP -> UpBad
    Trend.DOWN -> DownGood
    Trend.NEUTRAL -> TextPrimary
}
