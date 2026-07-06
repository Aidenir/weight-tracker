package com.aidenir.weighttracker.data

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus

data class Metrics(
    val current: Double?,
    val weekChangeKg: Double?,
    val monthChangeKg: Double?,
    val toGoalKg: Double?
)

fun computeMetrics(entries: List<WeightEntry>, goalKg: Double?): Metrics {
    if (entries.isEmpty()) {
        return Metrics(null, null, null, null)
    }
    val sorted = entries.sortedBy { it.date }
    val latest = sorted.last().kilograms
    val today = WeightRepository.today()
    val weekAgo = today.minus(7, DateTimeUnit.DAY)
    val monthAgo = today.minus(30, DateTimeUnit.DAY)

    val weekBase = sorted.lastOrNull { LocalDate.parse(it.date) <= weekAgo }?.kilograms
    val monthBase = sorted.lastOrNull { LocalDate.parse(it.date) <= monthAgo }?.kilograms

    return Metrics(
        current = latest,
        weekChangeKg = weekBase?.let { latest - it },
        monthChangeKg = monthBase?.let { latest - it },
        toGoalKg = goalKg?.let { latest - it }
    )
}
