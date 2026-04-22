package com.nearwake.domain.trip.engine

import com.nearwake.domain.trip.model.Confidence

sealed interface TripEvent {
    data object StartTrip : TripEvent

    data object MotionDetected : TripEvent

    data object ApproachGeofenceEntered : TripEvent

    data object NoMotionTimeoutReached : TripEvent

    data object UserConfirmedMoving : TripEvent

    data object ApproachWindowReached : TripEvent

    data object NetworkLost : TripEvent

    data class NetworkRestored(val confidence: Confidence = Confidence.HIGH) : TripEvent

    data object DestinationGeofenceEntered : TripEvent

    data object PreciseArrivalConfirmed : TripEvent

    data object AlertDismissed : TripEvent

    data object AlertTimedOut : TripEvent

    data object EndTrip : TripEvent

    data object RerouteRequested : TripEvent

    data object CancelTrip : TripEvent

    data object RestoreMonitoring : TripEvent
}
