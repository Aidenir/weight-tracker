package com.aidenir.weighttracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aidenir.weighttracker.data.Units
import com.aidenir.weighttracker.data.WeightEntry
import com.aidenir.weighttracker.data.WeightUnit
import com.aidenir.weighttracker.ui.WeightUiState
import com.aidenir.weighttracker.ui.components.GlassCard
import com.aidenir.weighttracker.ui.components.GlassIconButton
import com.aidenir.weighttracker.ui.components.WeightChart
import com.aidenir.weighttracker.ui.theme.DownGood
import com.aidenir.weighttracker.ui.theme.TextMuted
import com.aidenir.weighttracker.ui.theme.TextPrimary
import com.aidenir.weighttracker.ui.theme.TextSecondary
import com.aidenir.weighttracker.ui.theme.UpBad
import com.kyant.backdrop.Backdrop
import kotlinx.datetime.LocalDate
import java.time.format.DateTimeFormatter
import java.time.LocalDate as JLocalDate

@Composable
fun HistoryScreen(
    backdrop: Backdrop,
    state: WeightUiState,
    onDelete: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val unit = state.unit
    val sorted = state.entries.sortedByDescending { it.date }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = paddingValues(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "History",
                color = TextPrimary,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            GlassCard(backdrop = backdrop, contentPadding = 8.dp) {
                Column {
                    Text(
                        text = "Trend",
                        color = TextSecondary,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                    WeightChart(
                        entries = state.entries,
                        unit = unit,
                        goalKg = state.settings?.goal?.targetKg,
                        height = 220.dp
                    )
                }
            }
        }

        if (sorted.isEmpty()) {
            item {
                GlassCard(backdrop = backdrop) {
                    Text(
                        text = "No entries yet — log your first weigh-in.",
                        color = TextMuted,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            items(sorted, key = { it.date }) { entry ->
                HistoryRow(
                    backdrop = backdrop,
                    entry = entry,
                    prev = sorted.firstOrNull { it.date < entry.date },
                    unit = unit,
                    onDelete = { onDelete(LocalDate.parse(entry.date)) }
                )
            }
        }

        item { Spacer(Modifier.height(80.dp)) }
    }
}

@Composable
private fun paddingValues(): PaddingValues {
    val insets = WindowInsets.systemBars.asPaddingValues()
    return PaddingValues(
        start = 20.dp,
        end = 20.dp,
        top = insets.calculateTopPadding() + 8.dp,
        bottom = insets.calculateBottomPadding() + 8.dp
    )
}

@Composable
private fun HistoryRow(
    backdrop: Backdrop,
    entry: WeightEntry,
    prev: WeightEntry?,
    unit: WeightUnit,
    onDelete: () -> Unit
) {
    val delta = prev?.let { entry.kilograms - it.kilograms }
    val date = JLocalDate.parse(entry.date)
    val fmt = DateTimeFormatter.ofPattern("EEE d MMM")

    GlassCard(
        backdrop = backdrop,
        contentPadding = 16.dp
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = date.format(fmt),
                    color = TextSecondary,
                    style = MaterialTheme.typography.labelLarge
                )
                Text(
                    text = "${Units.format(entry.kilograms, unit)} ${Units.label(unit)}",
                    color = TextPrimary,
                    style = MaterialTheme.typography.titleLarge
                )
            }
            if (delta != null) {
                val color = when {
                    delta > 0.05 -> UpBad
                    delta < -0.05 -> DownGood
                    else -> TextMuted
                }
                Text(
                    text = "${Units.formatDelta(delta, unit)} ${Units.label(unit)}",
                    color = color,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
            GlassIconButton(
                backdrop = backdrop,
                onClick = onDelete
            ) {
                Icon(Icons.Rounded.Delete, contentDescription = "Delete", tint = TextPrimary)
            }
        }
    }
}
