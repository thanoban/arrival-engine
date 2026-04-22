package com.nearwake.domain.location.repository

import com.nearwake.domain.location.model.LatLng
import kotlinx.coroutines.flow.Flow

interface LocationRepository {
    suspend fun getLastKnownLocation(): LatLng?

    fun observeBalancedUpdates(): Flow<LatLng>

    fun observePreciseUpdates(): Flow<LatLng>

    suspend fun stopLocationUpdates()
}
