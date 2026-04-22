package com.nearwake.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.nearwake.core.database.entity.TripSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TripSessionDao {
    @Query("SELECT * FROM trip_sessions WHERE trip_id = :tripId LIMIT 1")
    fun observeTripSession(tripId: String): Flow<TripSessionEntity?>

    @Query("SELECT * FROM trip_sessions WHERE trip_id = :tripId LIMIT 1")
    suspend fun getTripSessionById(tripId: String): TripSessionEntity?

    @Query(
        """
        SELECT * FROM trip_sessions
        WHERE state NOT IN ('Completed', 'Cancelled', 'FailedGracefully')
        ORDER BY updated_at DESC
        LIMIT 1
        """,
    )
    suspend fun getActiveTripSession(): TripSessionEntity?

    @Upsert
    suspend fun upsertTripSession(session: TripSessionEntity)

    @Query("DELETE FROM trip_sessions WHERE trip_id = :tripId")
    suspend fun deleteTripSession(tripId: String)
}
