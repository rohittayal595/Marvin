package com.pulsar.marvin.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_logs")
data class DailyLog(
    @PrimaryKey
    val dateMillis: Long,
    val weight: Float?,
    val calories: Int?,
)
