package com.nearwake.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.datetime.Instant

@Entity(
    tableName = "diagnostics_events",
    indices = [Index("trip_id"), Index("event_type")],
)
data class DiagnosticsEventEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "trip_id")
    val tripId: String? = null,
    @ColumnInfo(name = "event_type")
    val eventType: String,
    @ColumnInfo(name = "payload_json")
    val payloadJson: String,
    @ColumnInfo(name = "recorded_at")
    val recordedAt: Instant,
)
