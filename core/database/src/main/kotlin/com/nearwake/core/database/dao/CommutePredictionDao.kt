package com.nearwake.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.nearwake.core.database.entity.CommutePredictionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CommutePredictionDao {
    @Query("SELECT * FROM commute_predictions ORDER BY trip_count DESC")
    fun observePredictions(): Flow<List<CommutePredictionEntity>>

    @Query("SELECT * FROM commute_predictions WHERE destination_id = :destinationId LIMIT 1")
    suspend fun getPredictionForDestination(destinationId: String): CommutePredictionEntity?

    @Upsert
    suspend fun upsertPredictions(predictions: List<CommutePredictionEntity>)

    @Query("DELETE FROM commute_predictions WHERE destination_id = :destinationId")
    suspend fun deletePredictionsForDestination(destinationId: String)

    @Query("DELETE FROM commute_predictions")
    suspend fun clearAll()
}
