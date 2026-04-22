package com.nearwake.domain.trip.usecase

import com.nearwake.domain.trip.engine.TripEngine
import com.nearwake.domain.trip.engine.TripEngineResult
import com.nearwake.domain.trip.engine.TripEvent
import com.nearwake.domain.trip.model.TripSession

class EnterRecoveryModeUseCase(
    private val tripEngine: TripEngine,
) {
    operator fun invoke(session: TripSession): TripEngineResult =
        tripEngine.onEvent(session = session, event = TripEvent.AlertTimedOut)
}
