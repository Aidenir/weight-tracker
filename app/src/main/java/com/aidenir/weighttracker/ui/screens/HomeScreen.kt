package com.aidenir.weighttracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aidenir.weighttracker.data.Units
import com.aidenir.weighttracker.ui.WeightUiState
import com.aidenir.weighttracker.ui.components.GlassCard
import com.aidenir.weighttracker.ui.components.MetricPill
import com.aidenir.weighttracker.ui.components.StepperButton
import com.aidenir.weighttracker.ui.components.StepperKind
import com.aidenir.weighttracker.ui.components.Trend
import com.aidenir.weighttracker.ui.components.WeightChart
import com.aidenir.weighttracker.ui.components.trendFor
import com.aidenir.weighttracker.ui.theme.BrandPrimary
import com.aidenir.weighttracker.ui.theme.TextMuted
import com.aidenir.weighttracker.ui.theme.TextPrimary
import com.aidenir.weighttracker.ui.theme.TextSecondary
import com.kyant.backdrop.Backdrop
import kotlin.math.max

@Composable
fun HomeScreen(
    backdrop: Backdrop,
    state: WeightUiState,
    onSave: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val unit = state.unit
    val prefill = state.todayKg ?: state.yesterdayKg ?: 75.0
    var draft by remember(prefill, unit) { mutableStateOf(Units.toDisplay(prefill, unit)) }
    LaunchedEffect(unit) {
        draft = Units.toDisplay(prefill, unit)
    }

    val step = Units.step(unit)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(WindowInsets.systemBars.asPaddingValues())
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Weigh in",
            color = TextPrimary,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )

        GlassCard(backdrop = backdrop, contentPadding = 24.dp) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val yLabel = state.yesterdayKg?.let {
                    "Yesterday: ${Units.format(it, unit)} ${Units.label(unit)}"
                } ?: "First weigh-in"
                Text(
                    text = yLabel,
                    color = TextMuted,
                    style = MaterialTheme.typography.labelMedium
                )
                Spacer(Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StepperButton(
                        backdrop = backdrop,
                        kind = StepperKind.MINUS,
                        onClick = { draft = max(0.0, roundStep(draft - step)) }
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        BasicTextField(
                            value = formatDraft(draft),
                            onValueChange = { txt ->
                                val cleaned = txt.replace(',', '.')
                                cleaned.toDoubleOrNull()?.let { draft = it }
                                    ?: run { if (cleaned.isBlank()) draft = 0.0 }
                            },
                            singleLine = true,
                            textStyle = TextStyle(
                                color = TextPrimary,
                                fontSize = 60.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            cursorBrush = SolidColor(BrandPrimary),
                            modifier = Modifier.width(200.dp)
                        )
                        Text(
                            text = Units.label(unit),
                            color = TextSecondary,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    StepperButton(
                        backdrop = backdrop,
                        kind = StepperKind.PLUS,
                        onClick = { draft = roundStep(draft + step) }
                    )
                }

                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = { onSave(Units.toKilograms(draft, unit)) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.15f),
                        contentColor = TextPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Icon(Icons.Rounded.Check, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Save today", fontWeight = FontWeight.SemiBold)
                }
            }
        }

        MetricsRow(backdrop = backdrop, state = state)

        GlassCard(backdrop = backdrop, contentPadding = 8.dp) {
            Column(Modifier.padding(vertical = 8.dp)) {
                Text(
                    text = "Progress",
                    color = TextSecondary,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                Spacer(Modifier.height(4.dp))
                WeightChart(
                    entries = state.entries,
                    unit = unit,
                    goalKg = state.settings?.goal?.targetKg
                )
            }
        }

        Spacer(Modifier.height(80.dp))
    }
}

@Composable
private fun MetricsRow(
    backdrop: Backdrop,
    state: WeightUiState
) {
    val unit = state.unit
    val m = state.metrics
    val current = m.current?.let { "${Units.format(it, unit)} ${Units.label(unit)}" } ?: "—"
    val week = m.weekChangeKg?.let { "${Units.formatDelta(it, unit)} ${Units.label(unit)}" } ?: "—"
    val month = m.monthChangeKg?.let { "${Units.formatDelta(it, unit)} ${Units.label(unit)}" } ?: "—"

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        MetricPill(
            backdrop = backdrop,
            label = "Current",
            value = current,
            modifier = Modifier.weight(1f),
            trend = Trend.NEUTRAL
        )
        MetricPill(
            backdrop = backdrop,
            label = "This week",
            value = week,
            modifier = Modifier.weight(1f),
            trend = trendFor(m.weekChangeKg)
        )
    }
    Spacer(Modifier.height(12.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        MetricPill(
            backdrop = backdrop,
            label = "This month",
            value = month,
            modifier = Modifier.weight(1f),
            trend = trendFor(m.monthChangeKg)
        )
        val toGoal = m.toGoalKg
        MetricPill(
            backdrop = backdrop,
            label = "To goal",
            value = toGoal?.let { "${Units.formatDelta(-it, unit)} ${Units.label(unit)}" } ?: "Set one",
            modifier = Modifier.weight(1f),
            trend = trendFor(toGoal?.let { -it })
        )
    }
}

private fun roundStep(v: Double): Double = kotlin.math.round(v * 10.0) / 10.0
private fun formatDraft(v: Double): String = String.format(java.util.Locale.US, "%.1f", v)
