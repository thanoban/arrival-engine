package com.nearwake.domain.trip.usecase

import com.nearwake.domain.trip.engine.TripEngine
import com.nearwake.domain.trip.engine.TripEngineResult
import com.nearwake.domain.trip.model.TripRule
import com.nearwake.domain.trip.model.TripSession

class EvaluateApproachUseCase(
    private val tripEngine: TripEngine,
) {
    operator fun invoke(
        session: TripSession,
        tripRule: TripRule,
        etaMinutes: Int?,
        distanceMeters: Double?,
    ): TripEngineResult = tripEngine.evaluateApproach(
        session = session,
        tripRule = tripRule,
        etaMinutes = etaMinutes,
        distanceMeters = distanceMeters,
    )
}
