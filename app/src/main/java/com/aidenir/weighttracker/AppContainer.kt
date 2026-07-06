package com.aidenir.weighttracker

import android.content.Context
import com.aidenir.weighttracker.data.SettingsRepository
import com.aidenir.weighttracker.data.WeightDatabase
import com.aidenir.weighttracker.data.WeightRepository

class AppContainer(private val context: Context) {
    val weightRepository: WeightRepository by lazy {
        WeightRepository(WeightDatabase.get(context).weightDao())
    }
    val settingsRepository: SettingsRepository by lazy { SettingsRepository(context) }
}
