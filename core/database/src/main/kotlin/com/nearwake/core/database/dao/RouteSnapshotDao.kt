package com.nearwake.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.nearwake.core.database.entity.RouteSnapshotEntity

@Dao
interface RouteSnapshotDao {
    @Query("SELECT * FROM route_snapshots WHERE trip_id = :tripId LIMIT 1")
    suspend fun getRouteSnapshotByTripId(tripId: String): RouteSnapshotEntity?

    @Upsert
    suspend fun upsertRouteSnapshot(snapshot: RouteSnapshotEntity)

    @Query("DELETE FROM route_snapshots WHERE trip_id = :tripId")
    suspend fun deleteRouteSnapshotByTripId(tripId: String)
}
