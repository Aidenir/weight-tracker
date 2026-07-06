package com.aidenir.weighttracker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [WeightEntry::class], version = 1, exportSchema = false)
abstract class WeightDatabase : RoomDatabase() {
    abstract fun weightDao(): WeightDao

    companion object {
        @Volatile private var instance: WeightDatabase? = null

        fun get(context: Context): WeightDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                WeightDatabase::class.java,
                "weight.db"
            ).fallbackToDestructiveMigration().build().also { instance = it }
        }
    }
}
