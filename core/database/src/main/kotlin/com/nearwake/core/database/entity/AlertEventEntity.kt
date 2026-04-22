package com.nearwake.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.nearwake.domain.trip.model.AlertType
import kotlinx.datetime.Instant

@Entity(
    tableName = "alert_events",
    foreignKeys = [
        ForeignKey(
            entity = TripEntity::class,
            parentColumns = ["id"],
            childColumns = ["trip_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("trip_id")],
)
data class AlertEventEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "trip_id")
    val tripId: String,
    @ColumnInfo(name = "fired_at")
    val firedAt: Instant,
    @ColumnInfo(name = "dismissed_at")
    val dismissedAt: Instant? = null,
    val type: AlertType,
)
