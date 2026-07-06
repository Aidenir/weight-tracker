package com.aidenir.weighttracker.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aidenir.weighttracker.WeightApp
import com.aidenir.weighttracker.data.HomeAssistantConfig
import com.aidenir.weighttracker.data.Metrics
import com.aidenir.weighttracker.data.ReminderConfig
import com.aidenir.weighttracker.data.Settings
import com.aidenir.weighttracker.data.WeightEntry
import com.aidenir.weighttracker.data.WeightRepository
import com.aidenir.weighttracker.data.WeightUnit
import com.aidenir.weighttracker.data.computeMetrics
import com.aidenir.weighttracker.notifications.ReminderScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

data class WeightUiState(
    val entries: List<WeightEntry> = emptyList(),
    val settings: Settings? = null,
    val metrics: Metrics = Metrics(null, null, null, null)
) {
    val unit: WeightUnit get() = settings?.unit ?: WeightUnit.KG
    val yesterdayKg: Double?
        get() {
            if (entries.isEmpty()) return null
            val today = WeightRepository.today().toString()
            return entries.lastOrNull { it.date < today }?.kilograms
                ?: entries.lastOrNull()?.kilograms
        }
    val todayKg: Double?
        get() {
            val today = WeightRepository.today().toString()
            return entries.firstOrNull { it.date == today }?.kilograms
        }
}

class WeightViewModel(app: Application) : AndroidViewModel(app) {

    private val container = (app as WeightApp).container
    private val weights = container.weightRepository
    private val settingsRepo = container.settingsRepository
    private val scheduler = ReminderScheduler(app)

    val state: StateFlow<WeightUiState> = combine(
        weights.observeAll(),
        settingsRepo.settings
    ) { list, s ->
        WeightUiState(
            entries = list,
            settings = s,
            metrics = computeMetrics(list, s.goal.targetKg)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), WeightUiState())

    fun saveToday(kg: Double) = viewModelScope.launch {
        weights.save(WeightRepository.today(), kg)
    }

    fun saveFor(date: LocalDate, kg: Double) = viewModelScope.launch {
        weights.save(date, kg)
    }

    fun delete(date: LocalDate) = viewModelScope.launch {
        weights.delete(date)
    }

    fun setUnit(unit: WeightUnit) = viewModelScope.launch { settingsRepo.setUnit(unit) }

    fun setGoal(kg: Double?, deadline: String?) = viewModelScope.launch {
        settingsRepo.setGoal(kg, deadline)
    }

    fun setReminder(config: ReminderConfig) = viewModelScope.launch {
        settingsRepo.setReminder(config)
        scheduler.reschedule(config)
    }

    fun setHomeAssistant(config: HomeAssistantConfig) = viewModelScope.launch {
        settingsRepo.setHomeAssistant(config)
    }

    fun scheduleReminderNow() = viewModelScope.launch {
        val s = state.value.settings ?: return@launch
        scheduler.reschedule(s.reminder)
    }
}
