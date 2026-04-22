package com.nearwake.data.location

import com.nearwake.domain.location.model.LatLng
import com.nearwake.domain.location.repository.LocationRepository
import javax.inject.Inject

class LocationRepositoryImpl @Inject constructor(
    private val fusedLocationDataSource: FusedLocationDataSource,
) : LocationRepository {
    override suspend fun getLastKnownLocation(): LatLng? =
        fusedLocationDataSource.getLastKnownLocation()

    override fun observeBalancedUpdates() =
        fusedLocationDataSource.startBalancedUpdates()

    override fun observePreciseUpdates() =
        fusedLocationDataSource.startPreciseBurst()

    override suspend fun stopLocationUpdates() {
        fusedLocationDataSource.stopUpdates()
    }
}
