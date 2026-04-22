package com.nearwake.domain.trip.usecase

import com.nearwake.domain.trip.engine.TripEngine
import com.nearwake.domain.trip.engine.TripEngineResult
import com.nearwake.domain.trip.model.Trip

class ArmTripUseCase(
    private val tripEngine: TripEngine,
) {
    operator fun invoke(trip: Trip, geofenceIds: List<String> = emptyList()): TripEngineResult =
        tripEngine.armTrip(trip = trip, geofenceIds = geofenceIds)
}
