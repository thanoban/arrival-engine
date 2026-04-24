package com.nearwake.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.nearwake.domain.trip.model.AlertStage
import com.nearwake.domain.trip.model.Confidence
import com.nearwake.domain.trip.model.MonitoringMode
import com.nearwake.domain.trip.model.TripState
import kotlinx.datetime.Instant

@Entity(
    tableName = "trip_sessions",
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
data class TripSessionEntity(
    @PrimaryKey
    @ColumnInfo(name = "trip_id")
    val tripId: String,
    val state: TripState,
    @ColumnInfo(name = "monitoring_mode")
    val monitoringMode: MonitoringMode,
    @ColumnInfo(name = "alert_stage")
    val alertStage: AlertStage = AlertStage.MONITORING,
    @ColumnInfo(name = "last_known_lat")
    val lastKnownLat: Double? = null,
    @ColumnInfo(name = "last_known_lng")
    val lastKnownLng: Double? = null,
    @ColumnInfo(name = "last_eta_minutes")
    val lastEtaMinutes: Int? = null,
    val confidence: Confidence,
    @ColumnInfo(name = "geofence_ids_json")
    val geofenceIds: List<String>,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Instant,
)
