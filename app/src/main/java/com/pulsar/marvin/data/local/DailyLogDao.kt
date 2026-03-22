package com.pulsar.marvin.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pulsar.marvin.data.model.DailyLog
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: DailyLog)

    @Query("SELECT * FROM daily_logs ORDER BY dateMillis DESC")
    fun getAllLogs(): Flow<List<DailyLog>>
    
    @Query("SELECT * FROM daily_logs WHERE dateMillis = :dateMillis LIMIT 1")
    suspend fun getLogByDate(dateMillis: Long): DailyLog?
}
