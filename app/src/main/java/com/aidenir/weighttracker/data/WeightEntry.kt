package com.aidenir.weighttracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weights")
data class WeightEntry(
    @PrimaryKey val date: String,
    val kilograms: Double,
    val recordedAtEpochMs: Long
)
