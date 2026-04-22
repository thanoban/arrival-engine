package com.nearwake.data.routing

import com.nearwake.domain.location.model.LatLng
import com.nearwake.domain.routing.RoutingDataSource
import com.nearwake.domain.routing.model.RouteSnapshot
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoOpRoutingDataSource @Inject constructor() : RoutingDataSource {
    override suspend fun fetchRoute(
        origin: LatLng,
        destination: LatLng,
    ): Result<RouteSnapshot> = Result.failure(
        UnsupportedOperationException(
            "Route-aware transit mode is not enabled in the MVP. NearWake is running destination-only mode.",
        ),
    )

    override suspend fun refreshEta(
        tripId: String,
        currentLocation: LatLng,
    ): Result<Int> = Result.failure(
        UnsupportedOperationException(
            "ETA refresh is unavailable in destination-only MVP mode.",
        ),
    )
}
