package com.nearwake.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.nearwake.core.database.entity.DiagnosticsEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DiagnosticsEventDao {
    @Query("SELECT * FROM diagnostics_events ORDER BY recorded_at DESC LIMIT :limit")
    fun observeRecentEvents(limit: Int = 200): Flow<List<DiagnosticsEventEntity>>

    @Query("SELECT * FROM diagnostics_events WHERE trip_id = :tripId ORDER BY recorded_at DESC")
    fun observeEventsForTrip(tripId: String): Flow<List<DiagnosticsEventEntity>>

    @Upsert
    suspend fun upsertEvent(event: DiagnosticsEventEntity)

    @Query("DELETE FROM diagnostics_events")
    suspend fun clearAll()
}
