package com.nearwake.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.nearwake.core.database.entity.SavedPlaceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedPlaceDao {
    @Query("SELECT * FROM saved_places ORDER BY last_used_at DESC, name ASC")
    fun observeSavedPlaces(): Flow<List<SavedPlaceEntity>>

    @Query("SELECT * FROM saved_places WHERE id = :id LIMIT 1")
    suspend fun getSavedPlaceById(id: String): SavedPlaceEntity?

    @Query("SELECT * FROM saved_places")
    suspend fun getAllSavedPlaces(): List<SavedPlaceEntity>

    @Upsert
    suspend fun upsertSavedPlace(place: SavedPlaceEntity)

    @Upsert
    suspend fun upsertSavedPlaces(places: List<SavedPlaceEntity>)

    @Delete
    suspend fun deleteSavedPlace(place: SavedPlaceEntity)
}
