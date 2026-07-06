package com.aidenir.weighttracker.data

import java.util.Locale
import kotlin.math.roundToInt

object Units {
    private const val KG_PER_LB = 0.45359237

    fun toDisplay(kg: Double, unit: WeightUnit): Double = when (unit) {
        WeightUnit.KG -> kg
        WeightUnit.LB -> kg / KG_PER_LB
    }

    fun toKilograms(value: Double, unit: WeightUnit): Double = when (unit) {
        WeightUnit.KG -> value
        WeightUnit.LB -> value * KG_PER_LB
    }

    fun label(unit: WeightUnit): String = when (unit) {
        WeightUnit.KG -> "kg"
        WeightUnit.LB -> "lb"
    }

    fun format(kg: Double, unit: WeightUnit, digits: Int = 1): String {
        val v = toDisplay(kg, unit)
        val factor = Math.pow(10.0, digits.toDouble())
        val rounded = (v * factor).roundToInt() / factor
        return String.format(Locale.US, "%.${digits}f", rounded)
    }

    fun formatDelta(deltaKg: Double, unit: WeightUnit, digits: Int = 1): String {
        val sign = if (deltaKg > 0) "+" else if (deltaKg < 0) "-" else ""
        return sign + format(kotlin.math.abs(deltaKg), unit, digits)
    }

    fun step(unit: WeightUnit): Double = when (unit) {
        WeightUnit.KG -> 0.1
        WeightUnit.LB -> 0.2
    }
}
