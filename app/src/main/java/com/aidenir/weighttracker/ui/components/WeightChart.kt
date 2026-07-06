package com.aidenir.weighttracker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aidenir.weighttracker.data.Units
import com.aidenir.weighttracker.data.WeightEntry
import com.aidenir.weighttracker.data.WeightUnit
import com.aidenir.weighttracker.ui.theme.BrandAccent
import com.aidenir.weighttracker.ui.theme.BrandPrimary
import com.aidenir.weighttracker.ui.theme.BrandSecondary
import com.aidenir.weighttracker.ui.theme.TextMuted
import kotlinx.datetime.LocalDate
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

@Composable
fun WeightChart(
    entries: List<WeightEntry>,
    unit: WeightUnit,
    modifier: Modifier = Modifier,
    height: Dp = 200.dp,
    goalKg: Double? = null
) {
    if (entries.isEmpty()) {
        Column(modifier.padding(16.dp)) {
            Text(
                text = "Not enough data yet",
                color = TextMuted,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(height / 2))
        }
        return
    }

    val sorted = remember(entries) { entries.sortedBy { it.date } }
    val minKg = sorted.minOf { it.kilograms }
    val maxKg = sorted.maxOf { it.kilograms }
    val goalTouches = goalKg?.let { it in (minKg - 0.001)..(maxKg + 0.001) } ?: false
    val lo = min(minKg, goalKg ?: minKg) - 0.5
    val hi = max(maxKg, goalKg ?: maxKg) + 0.5
    val range = (hi - lo).coerceAtLeast(1.0)

    val firstDate = LocalDate.parse(sorted.first().date)
    val lastDate = LocalDate.parse(sorted.last().date)
    val spanDays = max(1, lastDate.toEpochDays() - firstDate.toEpochDays())

    Column(modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .padding(horizontal = 8.dp)
        ) {
            val w = size.width
            val h = size.height
            val paddingLeft = 8.dp.toPx()
            val paddingRight = 8.dp.toPx()
            val paddingTop = 12.dp.toPx()
            val paddingBottom = 20.dp.toPx()
            val innerW = w - paddingLeft - paddingRight
            val innerH = h - paddingTop - paddingBottom

            fun xFor(date: LocalDate): Float {
                val d = (date.toEpochDays() - firstDate.toEpochDays()).toFloat()
                return paddingLeft + (d / spanDays) * innerW
            }
            fun yFor(kg: Double): Float {
                val frac = ((kg - lo) / range).toFloat()
                return paddingTop + (1f - frac) * innerH
            }

            // Grid lines
            val gridColor = Color.White.copy(alpha = 0.08f)
            for (i in 0..3) {
                val y = paddingTop + innerH * i / 3f
                drawLine(
                    color = gridColor,
                    start = Offset(paddingLeft, y),
                    end = Offset(paddingLeft + innerW, y),
                    strokeWidth = 1f
                )
            }

            // Goal reference line
            if (goalKg != null) {
                val gy = yFor(goalKg)
                drawLine(
                    color = BrandAccent.copy(alpha = 0.6f),
                    start = Offset(paddingLeft, gy),
                    end = Offset(paddingLeft + innerW, gy),
                    strokeWidth = 1.5f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                )
            }

            val points = sorted.map { Offset(xFor(LocalDate.parse(it.date)), yFor(it.kilograms)) }

            // Build a smooth path (Catmull-Rom-ish via mid-point cubic).
            val linePath = Path().apply {
                if (points.isNotEmpty()) {
                    moveTo(points.first().x, points.first().y)
                    for (i in 1 until points.size) {
                        val prev = points[i - 1]
                        val cur = points[i]
                        val midX = (prev.x + cur.x) / 2f
                        cubicTo(midX, prev.y, midX, cur.y, cur.x, cur.y)
                    }
                }
            }

            // Fill under curve
            val fillPath = Path().apply {
                addPath(linePath)
                if (points.isNotEmpty()) {
                    lineTo(points.last().x, paddingTop + innerH)
                    lineTo(points.first().x, paddingTop + innerH)
                    close()
                }
            }
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(BrandPrimary.copy(alpha = 0.45f), Color.Transparent),
                    startY = paddingTop,
                    endY = paddingTop + innerH
                )
            )

            drawPath(
                path = linePath,
                brush = Brush.horizontalGradient(
                    colors = listOf(BrandSecondary, BrandPrimary, BrandAccent)
                ),
                style = Stroke(width = 3.5f)
            )

            // Points
            points.forEach { p ->
                drawCircle(
                    color = Color.White,
                    radius = 3.5f,
                    center = p
                )
                drawCircle(
                    color = BrandPrimary,
                    radius = 2f,
                    center = p
                )
            }
        }

        // Range labels
        Text(
            text = buildString {
                append("Range: ")
                append(Units.format(minKg, unit))
                append(" – ")
                append(Units.format(maxKg, unit))
                append(" ")
                append(Units.label(unit))
                if (goalKg != null && !goalTouches) {
                    append("   •   Goal: ")
                    append(Units.format(goalKg, unit))
                    append(" ")
                    append(Units.label(unit))
                }
            },
            color = TextMuted,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )
    }
}

