package com.nearwake.domain.trip.usecase

import com.nearwake.domain.trip.engine.OvershootDetector
import com.nearwake.domain.trip.model.TripRule

class DetectOvershootUseCase(
    private val overshootDetector: OvershootDetector,
) {
    operator fun invoke(
        previousDistanceMeters: Double?,
        currentDistanceMeters: Double?,
        tripRule: TripRule,
    ): Boolean = overshootDetector.hasOvershot(
        previousDistanceMeters = previousDistanceMeters,
        currentDistanceMeters = currentDistanceMeters,
        tripRule = tripRule,
    )
}
