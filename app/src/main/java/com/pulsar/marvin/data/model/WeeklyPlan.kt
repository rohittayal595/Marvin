package com.pulsar.marvin.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weekly_plans")
data class WeeklyPlan(
    @PrimaryKey
    val startOfWeekMillis: Long,
    val weekNumber: Int,
    val targetWeight: Float,
)
