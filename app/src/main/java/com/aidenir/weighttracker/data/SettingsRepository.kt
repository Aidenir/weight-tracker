package com.aidenir.weighttracker.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class WeightUnit { KG, LB }

enum class Gender { FEMALE, MALE, UNSPECIFIED }

data class GoalConfig(
    val targetKg: Double?,
    val deadline: String?,
    val milestoneKgs: List<Double> = emptyList()
)

data class ReminderConfig(
    val enabled: Boolean,
    val hour: Int,
    val minute: Int,
    val useMotion: Boolean
)

data class HomeAssistantConfig(
    val baseUrl: String,
    val token: String,
    val bedEntity: String
) {
    fun isConfigured(): Boolean =
        baseUrl.isNotBlank() && token.isNotBlank() && bedEntity.isNotBlank()
}

data class Profile(
    val ageYears: Int?,
    val gender: Gender
)

data class Settings(
    val unit: WeightUnit,
    val goal: GoalConfig,
    val reminder: ReminderConfig,
    val homeAssistant: HomeAssistantConfig,
    val profile: Profile
)

private val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {

    private val store get() = context.dataStore

    val settings: Flow<Settings> = store.data.map { prefs ->
        Settings(
            unit = if (prefs[K_UNIT] == "LB") WeightUnit.LB else WeightUnit.KG,
            goal = GoalConfig(
                targetKg = prefs[K_GOAL_KG],
                deadline = prefs[K_GOAL_DEADLINE],
                milestoneKgs = parseMilestones(prefs[K_GOAL_MILESTONES])
            ),
            reminder = ReminderConfig(
                enabled = prefs[K_REMINDER_ENABLED] ?: true,
                hour = prefs[K_REMINDER_HOUR] ?: 7,
                minute = prefs[K_REMINDER_MIN] ?: 30,
                useMotion = prefs[K_USE_MOTION] ?: false
            ),
            homeAssistant = HomeAssistantConfig(
                baseUrl = prefs[K_HA_URL].orEmpty(),
                token = prefs[K_HA_TOKEN].orEmpty(),
                bedEntity = prefs[K_HA_ENTITY].orEmpty()
            ),
            profile = Profile(
                ageYears = prefs[K_AGE]?.takeIf { it in 10..120 },
                gender = when (prefs[K_GENDER]) {
                    "FEMALE" -> Gender.FEMALE
                    "MALE" -> Gender.MALE
                    else -> Gender.UNSPECIFIED
                }
            )
        )
    }

    suspend fun setUnit(unit: WeightUnit) =
        store.edit { it[K_UNIT] = unit.name }

    suspend fun setGoal(kg: Double?, deadline: String?) = store.edit {
        if (kg == null) it.remove(K_GOAL_KG) else it[K_GOAL_KG] = kg
        if (deadline.isNullOrBlank()) it.remove(K_GOAL_DEADLINE) else it[K_GOAL_DEADLINE] = deadline
    }

    suspend fun setMilestones(kgs: List<Double>) = store.edit {
        if (kgs.isEmpty()) {
            it.remove(K_GOAL_MILESTONES)
        } else {
            it[K_GOAL_MILESTONES] = kgs.joinToString(",") { kg -> "%.2f".format(kg) }
        }
    }

    suspend fun setReminder(config: ReminderConfig) = store.edit {
        it[K_REMINDER_ENABLED] = config.enabled
        it[K_REMINDER_HOUR] = config.hour
        it[K_REMINDER_MIN] = config.minute
        it[K_USE_MOTION] = config.useMotion
    }

    suspend fun setHomeAssistant(config: HomeAssistantConfig) = store.edit {
        it[K_HA_URL] = config.baseUrl
        it[K_HA_TOKEN] = config.token
        it[K_HA_ENTITY] = config.bedEntity
    }

    suspend fun setProfile(profile: Profile) = store.edit {
        val age = profile.ageYears
        if (age == null) it.remove(K_AGE) else it[K_AGE] = age
        it[K_GENDER] = profile.gender.name
    }

    private fun parseMilestones(raw: String?): List<Double> {
        if (raw.isNullOrBlank()) return emptyList()
        return raw.split(',').mapNotNull { it.trim().toDoubleOrNull() }
    }

    private companion object {
        val K_UNIT = stringPreferencesKey("unit")
        val K_GOAL_KG = doublePreferencesKey("goal_kg")
        val K_GOAL_DEADLINE = stringPreferencesKey("goal_deadline")
        val K_GOAL_MILESTONES = stringPreferencesKey("goal_milestones")
        val K_REMINDER_ENABLED = booleanPreferencesKey("reminder_enabled")
        val K_REMINDER_HOUR = intPreferencesKey("reminder_hour")
        val K_REMINDER_MIN = intPreferencesKey("reminder_min")
        val K_USE_MOTION = booleanPreferencesKey("use_motion")
        val K_HA_URL = stringPreferencesKey("ha_url")
        val K_HA_TOKEN = stringPreferencesKey("ha_token")
        val K_HA_ENTITY = stringPreferencesKey("ha_entity")
        val K_AGE = intPreferencesKey("profile_age")
        val K_GENDER = stringPreferencesKey("profile_gender")
    }
}
