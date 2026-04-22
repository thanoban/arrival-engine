package com.nearwake.domain.trip.usecase

import com.nearwake.domain.trip.engine.TripEngine
import com.nearwake.domain.trip.engine.TripEngineResult
import com.nearwake.domain.trip.model.TripRule
import com.nearwake.domain.trip.model.TripSession

class FireArrivalAlertUseCase(
    private val tripEngine: TripEngine,
) {
    operator fun invoke(
        session: TripSession,
        tripRule: TripRule,
        distanceMeters: Double?,
        previousDistanceMeters: Double?,
        destinationGeofenceEntered: Boolean,
    ): TripEngineResult = tripEngine.evaluateAlert(
        session = session,
        tripRule = tripRule,
        distanceMeters = distanceMeters,
        previousDistanceMeters = previousDistanceMeters,
        destinationGeofenceEntered = destinationGeofenceEntered,
    )
}
