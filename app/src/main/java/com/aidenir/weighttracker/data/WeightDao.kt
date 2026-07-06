package com.aidenir.weighttracker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WeightDao {

    @Query("SELECT * FROM weights ORDER BY date ASC")
    fun observeAll(): Flow<List<WeightEntry>>

    @Query("SELECT * FROM weights ORDER BY date DESC LIMIT 1")
    suspend fun latest(): WeightEntry?

    @Query("SELECT * FROM weights WHERE date = :date LIMIT 1")
    suspend fun forDate(date: String): WeightEntry?

    @Query("SELECT * FROM weights WHERE date < :date ORDER BY date DESC LIMIT 1")
    suspend fun latestBefore(date: String): WeightEntry?

    @Query("SELECT * FROM weights WHERE date >= :from ORDER BY date ASC")
    suspend fun since(from: String): List<WeightEntry>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: WeightEntry)

    @Query("DELETE FROM weights WHERE date = :date")
    suspend fun deleteByDate(date: String)
}
