package com.aidenir.weighttracker.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
        shape = RoundedCornerShape(18.dp),
        blurRadius = 18.dp,
        contentPadding = 14.dp
    ) {
        Column {
            Text(
                text = label,
                color = TextMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(6.dp))
            val color = when (trend) {
                Trend.UP -> UpBad
                Trend.DOWN -> DownGood
                Trend.NEUTRAL -> TextPrimary
            }
            Text(
                text = value,
                color = color,
                style = MaterialTheme.typography.titleLarge
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
