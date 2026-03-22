package com.pulsar.marvin.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.pulsar.marvin.data.model.DailyLog
import com.pulsar.marvin.data.model.WeeklyPlan

@Database(entities = [DailyLog::class, WeeklyPlan::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dailyLogDao(): DailyLogDao
    abstract fun weeklyPlanDao(): WeeklyPlanDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "marvin_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
