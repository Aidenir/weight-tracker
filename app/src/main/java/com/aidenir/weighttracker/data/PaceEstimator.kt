package com.aidenir.weighttracker.data

import kotlin.math.abs

/**
 * A conservative "healthy pace" estimator for how long it should take to move
 * from [currentKg] to a target weight, given a coarse profile.
 *
 * The numbers here are heuristics, not clinical advice — the goal is to give
 * the user a defensible ETA rather than let them pick a random deadline.
 *
 * Model:
 *   - Base weekly rate for weight loss is ~1% of body weight per week
 *     (the low-risk upper end of the CDC / NHS guidance), clamped to
 *     [0.35, 0.75] kg/wk so extremes don't dominate.
 *   - For weight gain the base is 0.35 kg/wk — you can't add lean mass
 *     much faster than that; anything more is mostly fat.
 *   - Age damps the rate a little per decade past 30, reflecting the
 *     gradual BMR decline.
 *   - Gender damps slightly for female-typical body composition (lower
 *     lean mass fraction → smaller absolute weekly delta at the same
 *     relative rate); UNSPECIFIED sits between the two.
 *
 * Callers should present the output as an approximation ("at a healthy pace
 * you'd reach 70 kg around Aug 4"), not as a promise.
 */
object PaceEstimator {

    /** Weekly rate in kg/week we'd expect the user to sustain safely. */
    fun weeklyRateKg(currentKg: Double, targetKg: Double, profile: Profile): Double {
        val losing = targetKg < currentKg
        val base = if (losing) {
            (currentKg * 0.01).coerceIn(0.35, 0.75)
        } else {
            0.35
        }
        return base * genderFactor(profile.gender) * ageFactor(profile.ageYears)
    }

    /**
     * Days from now to reach [targetKg] at the estimated weekly rate.
     * Returns null when the target equals the current weight (already there)
     * or the rate collapses to zero (shouldn't happen but be defensive).
     */
    fun daysToTarget(currentKg: Double, targetKg: Double, profile: Profile): Int? {
        val delta = abs(currentKg - targetKg)
        if (delta < 0.05) return 0
        val rate = weeklyRateKg(currentKg, targetKg, profile)
        if (rate <= 0.0) return null
        val weeks = delta / rate
        return (weeks * 7.0).toInt().coerceAtLeast(1)
    }

    private fun genderFactor(gender: Gender): Double = when (gender) {
        Gender.MALE -> 1.00
        Gender.FEMALE -> 0.90
        Gender.UNSPECIFIED -> 0.95
    }

    private fun ageFactor(age: Int?): Double {
        if (age == null) return 0.95
        return when {
            age <= 30 -> 1.00
            age <= 40 -> 0.95
            age <= 50 -> 0.90
            age <= 60 -> 0.85
            else -> 0.80
        }
    }
}
