package com.nearwake.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.Instant

@Entity(tableName = "saved_places")
data class SavedPlaceEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val address: String,
    val lat: Double,
    val lng: Double,
    @ColumnInfo(name = "place_id")
    val placeId: String? = null,
    @ColumnInfo(name = "last_used_at")
    val lastUsedAt: Instant? = null,
)
