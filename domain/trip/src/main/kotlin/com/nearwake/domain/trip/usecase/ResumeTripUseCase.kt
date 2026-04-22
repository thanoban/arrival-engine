package com.nearwake.domain.trip.usecase

import com.nearwake.domain.trip.engine.TripEngine
import com.nearwake.domain.trip.engine.TripEngineResult
import com.nearwake.domain.trip.model.TripSession

class ResumeTripUseCase(
    private val tripEngine: TripEngine,
) {
    operator fun invoke(session: TripSession): TripEngineResult =
        tripEngine.restoreSession(session = session)
}
