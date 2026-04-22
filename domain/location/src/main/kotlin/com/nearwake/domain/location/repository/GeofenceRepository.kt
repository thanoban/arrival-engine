package com.nearwake.domain.location.repository

import com.nearwake.domain.location.model.GeofenceSpec

interface GeofenceRepository {
    suspend fun registerGeofences(specs: List<GeofenceSpec>)

    suspend fun removeGeofences(ids: List<String>)

    suspend fun removeAllGeofences()
}
