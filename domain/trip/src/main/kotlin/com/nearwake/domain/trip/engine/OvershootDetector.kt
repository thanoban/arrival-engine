package com.nearwake.domain.trip.engine

import com.nearwake.domain.trip.model.TripRule

class OvershootDetector(
    private val overshootSlackMeters: Double = DEFAULT_OVERSHOOT_SLACK_METERS,
) {
    fun hasOvershot(
        previousDistanceMeters: Double?,
        currentDistanceMeters: Double?,
        tripRule: TripRule,
    ): Boolean {
        if (previousDistanceMeters == null || currentDistanceMeters == null) {
            return false
        }

        val wasInArrivalWindow = previousDistanceMeters <= tripRule.destinationRadiusMeters + overshootSlackMeters
        val movedAwayFromDestination = currentDistanceMeters > previousDistanceMeters + overshootSlackMeters
        return wasInArrivalWindow && movedAwayFromDestination
    }

    companion object {
        const val DEFAULT_OVERSHOOT_SLACK_METERS = 150.0
    }
}
