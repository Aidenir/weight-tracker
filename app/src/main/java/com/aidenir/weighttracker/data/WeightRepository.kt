package com.aidenir.weighttracker.data

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime

class WeightRepository(private val dao: WeightDao) {

    fun observeAll(): Flow<List<WeightEntry>> = dao.observeAll()

    suspend fun forDate(date: LocalDate) = dao.forDate(date.toString())

    suspend fun latest(): WeightEntry? = dao.latest()

    suspend fun latestBefore(date: LocalDate): WeightEntry? = dao.latestBefore(date.toString())

    suspend fun since(days: Int): List<WeightEntry> {
        val from = today().minus(days, DateTimeUnit.DAY)
        return dao.since(from.toString())
    }

    suspend fun save(date: LocalDate, kilograms: Double) {
        dao.upsert(
            WeightEntry(
                date = date.toString(),
                kilograms = kilograms,
                recordedAtEpochMs = Clock.System.now().toEpochMilliseconds()
            )
        )
    }

    suspend fun delete(date: LocalDate) = dao.deleteByDate(date.toString())

    companion object {
        fun today(): LocalDate =
            Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    }
}
