package com.aidenir.weighttracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aidenir.weighttracker.data.Units
import com.aidenir.weighttracker.ui.WeightUiState
import com.aidenir.weighttracker.ui.components.GlassCard
import com.aidenir.weighttracker.ui.components.MetricPill
import com.aidenir.weighttracker.ui.components.Trend
import com.aidenir.weighttracker.ui.components.bottomActionClearance
import com.aidenir.weighttracker.ui.theme.BrandPrimary
import com.aidenir.weighttracker.ui.theme.TextMuted
import com.aidenir.weighttracker.ui.theme.TextPrimary
import com.aidenir.weighttracker.ui.theme.TextSecondary
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.math.abs
import com.kyant.backdrop.Backdrop

@Composable
fun GoalScreen(
    backdrop: Backdrop,
    state: WeightUiState,
    onSave: (Double?, String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val unit = state.unit
    val goal = state.settings?.goal

    var targetInput by remember(goal?.targetKg, unit) {
        mutableStateOf(goal?.targetKg?.let { Units.format(it, unit) } ?: "")
    }
    var deadlineInput by remember(goal?.deadline) { mutableStateOf(goal?.deadline.orEmpty()) }

    LaunchedEffect(unit) {
        targetInput = goal?.targetKg?.let { Units.format(it, unit) } ?: ""
    }

    val bottomClearance = bottomActionClearance()

    Box(modifier.fillMaxSize().imePadding()) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(
                    top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 8.dp,
                    start = 20.dp,
                    end = 20.dp
                ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Goal",
                color = TextSecondary,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.SemiBold
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                val toGo = state.metrics.toGoalKg
                MetricPill(
                    backdrop = backdrop,
                    label = "To goal",
                    value = toGo?.let { "${Units.formatDelta(-it, unit)} ${Units.label(unit)}" } ?: "—",
                    modifier = Modifier.weight(1f),
                    trend = when {
                        toGo == null -> Trend.NEUTRAL
                        toGo > 0.05 -> Trend.UP
                        toGo < -0.05 -> Trend.DOWN
                        else -> Trend.NEUTRAL
                    }
                )
                val pace = paceForecast(state)
                MetricPill(
                    backdrop = backdrop,
                    label = "Weekly pace",
                    value = pace?.let { "${Units.formatDelta(it, unit)} ${Units.label(unit)}/wk" } ?: "—",
                    modifier = Modifier.weight(1f),
                    trend = if (pace == null) Trend.NEUTRAL else if (pace < 0) Trend.DOWN else Trend.UP
                )
            }

            val eta = etaMessage(state)
            if (eta != null) {
                GlassCard(backdrop = backdrop, contentPadding = 20.dp) {
                    Text(
                        text = eta,
                        color = TextPrimary,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            Spacer(Modifier.height(bottomClearance + 220.dp))
        }

        // Sticky bottom input cluster
        GoalInputCard(
            backdrop = backdrop,
            unit = unit,
            targetInput = targetInput,
            onTargetChange = { targetInput = it },
            deadlineInput = deadlineInput,
            onDeadlineChange = { deadlineInput = it },
            onSave = {
                val kg = targetInput.replace(',', '.').toDoubleOrNull()
                    ?.let { Units.toKilograms(it, unit) }
                val deadline = deadlineInput.takeIf { it.matches(Regex("\\d{4}-\\d{2}-\\d{2}")) }
                onSave(kg, deadline)
            },
            onClear = {
                targetInput = ""
                deadlineInput = ""
                onSave(null, null)
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = bottomClearance)
        )
    }
}

@Composable
private fun GoalInputCard(
    backdrop: Backdrop,
    unit: com.aidenir.weighttracker.data.WeightUnit,
    targetInput: String,
    onTargetChange: (String) -> Unit,
    deadlineInput: String,
    onDeadlineChange: (String) -> Unit,
    onSave: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(
        backdrop = backdrop,
        modifier = modifier,
        contentPadding = 20.dp,
        blurRadius = 30.dp,
        tint = Color.White.copy(alpha = 0.14f)
    ) {
        Column {
            Text("Target weight", color = TextMuted, style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                BasicTextField(
                    value = targetInput,
                    onValueChange = { txt ->
                        onTargetChange(txt.filter { c -> c.isDigit() || c == '.' || c == ',' })
                    },
                    singleLine = true,
                    textStyle = TextStyle(
                        color = TextPrimary,
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    cursorBrush = SolidColor(BrandPrimary),
                    modifier = Modifier.width(140.dp)
                )
                Text(
                    Units.label(unit),
                    color = TextSecondary,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(Modifier.height(12.dp))
            Text("Deadline (YYYY-MM-DD, optional)", color = TextMuted, style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(6.dp))
            BasicTextField(
                value = deadlineInput,
                onValueChange = { onDeadlineChange(it.take(10)) },
                singleLine = true,
                textStyle = TextStyle(color = TextPrimary, fontSize = 18.sp),
                cursorBrush = SolidColor(BrandPrimary),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onSave,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.12f),
                        contentColor = TextPrimary
                    )
                ) {
                    Text("Save goal", fontWeight = FontWeight.SemiBold)
                }
                TextButton(
                    onClick = onClear,
                    modifier = Modifier.height(52.dp)
                ) {
                    Text("Clear", color = TextSecondary)
                }
            }
        }
    }
}

private fun paceForecast(state: WeightUiState): Double? {
    val entries = state.entries.sortedBy { it.date }
    if (entries.size < 2) return null
    val recent = entries.takeLast(21)
    if (recent.size < 2) return null
    val first = recent.first()
    val last = recent.last()
    val days = LocalDate.parse(last.date).toEpochDays() - LocalDate.parse(first.date).toEpochDays()
    if (days <= 0) return null
    val perDay = (last.kilograms - first.kilograms) / days
    return perDay * 7
}

private fun etaMessage(state: WeightUiState): String? {
    val goal = state.settings?.goal?.targetKg ?: return null
    val current = state.metrics.current ?: return null
    val pacePerWeek = paceForecast(state) ?: return null
    val diff = current - goal
    if (abs(diff) < 0.1) return "You are at your goal — nice."
    if (pacePerWeek == 0.0) return "Your trend is flat — pick a plan and try again."
    val weeksNeeded = diff / -pacePerWeek
    if (weeksNeeded < 0) return "Current pace is moving away from your goal."
    val days = (weeksNeeded * 7).toInt()
    val lastDate = LocalDate.parse(state.entries.last().date)
    val target = lastDate.plus(days, DateTimeUnit.DAY)
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    val until = today.daysUntil(target)
    return "At this pace you reach ${Units.format(goal, state.unit)} ${Units.label(state.unit)} in about $until days."
}
