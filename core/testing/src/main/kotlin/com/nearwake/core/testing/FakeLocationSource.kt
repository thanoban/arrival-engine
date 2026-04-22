package com.nearwake.core.testing

import com.nearwake.domain.location.model.LatLng
import com.nearwake.domain.location.repository.LocationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

class FakeLocationSource(
    private var lastKnownLocation: LatLng? = null,
) : LocationRepository {
    private val balancedUpdates = MutableSharedFlow<LatLng>(extraBufferCapacity = 8)
    private val preciseUpdates = MutableSharedFlow<LatLng>(extraBufferCapacity = 8)

    var stopUpdatesCallCount: Int = 0
        private set

    override suspend fun getLastKnownLocation(): LatLng? = lastKnownLocation

    override fun observeBalancedUpdates(): Flow<LatLng> = balancedUpdates

    override fun observePreciseUpdates(): Flow<LatLng> = preciseUpdates

    override suspend fun stopLocationUpdates() {
        stopUpdatesCallCount += 1
    }

    fun setLastKnownLocation(location: LatLng?) {
        lastKnownLocation = location
    }

    suspend fun emitBalancedUpdate(location: LatLng) {
        lastKnownLocation = location
        balancedUpdates.emit(location)
    }

    suspend fun emitPreciseUpdate(location: LatLng) {
        lastKnownLocation = location
        preciseUpdates.emit(location)
    }
}
