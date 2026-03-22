package com.pulsar.marvin.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pulsar.marvin.data.model.WeeklyPlan
import kotlinx.coroutines.flow.Flow

@Dao
interface WeeklyPlanDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(plans: List<WeeklyPlan>)

    @Query("SELECT * FROM weekly_plans ORDER BY startOfWeekMillis")
    fun getAllPlans(): Flow<List<WeeklyPlan>>
    
    @Query("DELETE FROM weekly_plans")
    suspend fun deleteAll()
}
