package com.nearwake.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.Instant

@Entity(tableName = "route_snapshots")
data class RouteSnapshotEntity(
    @PrimaryKey
    @ColumnInfo(name = "trip_id")
    val tripId: String,
    @ColumnInfo(name = "stops_json")
    val stopsJson: String,
    @ColumnInfo(name = "transfers_json")
    val transfersJson: String,
    @ColumnInfo(name = "total_duration_minutes")
    val totalDurationMinutes: Int,
    @ColumnInfo(name = "fetched_at")
    val fetchedAt: Instant,
    @ColumnInfo(name = "is_stale")
    val isStale: Boolean,
)
