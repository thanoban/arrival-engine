package com.nearwake.data.location

import com.nearwake.domain.location.model.GeofenceSpec
import com.nearwake.domain.location.repository.GeofenceRepository
import javax.inject.Inject

class GeofenceRepositoryImpl @Inject constructor(
    private val geofenceDataSource: GeofenceDataSource,
) : GeofenceRepository {
    override suspend fun registerGeofences(specs: List<GeofenceSpec>) {
        geofenceDataSource.registerGeofences(specs).getOrThrow()
    }

    override suspend fun removeGeofences(ids: List<String>) {
        geofenceDataSource.removeGeofences(ids).getOrThrow()
    }

    override suspend fun removeAllGeofences() {
        geofenceDataSource.removeAllGeofences().getOrThrow()
    }
}
