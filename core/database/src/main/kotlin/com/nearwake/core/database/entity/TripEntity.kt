package com.nearwake.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.nearwake.domain.trip.model.AlertIntensity
import kotlinx.datetime.Instant

@Entity(
    tableName = "trips",
    foreignKeys = [
        ForeignKey(
            entity = SavedPlaceEntity::class,
            parentColumns = ["id"],
            childColumns = ["destination_id"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [Index("destination_id")],
)
data class TripEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "destination_id")
    val destinationId: String,
    @ColumnInfo(name = "alert_lead_min")
    val alertLeadMinutes: Int,
    @ColumnInfo(name = "alert_intensity")
    val alertIntensity: AlertIntensity,
    @ColumnInfo(name = "created_at")
    val createdAt: Instant,
    @ColumnInfo(name = "completed_at")
    val completedAt: Instant? = null,
)
