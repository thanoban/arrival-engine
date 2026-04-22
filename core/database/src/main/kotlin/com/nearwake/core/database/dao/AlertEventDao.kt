package com.nearwake.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.nearwake.core.database.entity.AlertEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AlertEventDao {
    @Query("SELECT * FROM alert_events WHERE trip_id = :tripId ORDER BY fired_at DESC")
    fun observeAlertEventsForTrip(tripId: String): Flow<List<AlertEventEntity>>

    @Query("SELECT * FROM alert_events WHERE id = :eventId LIMIT 1")
    suspend fun getAlertEventById(eventId: String): AlertEventEntity?

    @Upsert
    suspend fun upsertAlertEvent(event: AlertEventEntity)
}
