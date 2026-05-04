package com.nearwake.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.datetime.Instant

@Entity(
    tableName = "commute_predictions",
    indices = [
        Index(value = ["destination_id"]),
    ],
)
data class CommutePredictionEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "origin_id")
    val originId: String? = null,
    @ColumnInfo(name = "destination_id")
    val destinationId: String,
    @ColumnInfo(name = "destination_name")
    val destinationName: String,
    @ColumnInfo(name = "days_of_week_json")
    val daysOfWeekJson: String,
    @ColumnInfo(name = "typical_departure_hour")
    val typicalDepartureHour: Int,
    @ColumnInfo(name = "typical_departure_minute")
    val typicalDepartureMinute: Int,
    @ColumnInfo(name = "avg_duration_minutes")
    val avgDurationMinutes: Int,
    @ColumnInfo(name = "trip_count")
    val tripCount: Int,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Instant,
)
