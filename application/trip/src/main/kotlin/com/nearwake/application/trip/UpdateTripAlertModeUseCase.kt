package com.nearwake.application.trip

import com.nearwake.domain.trip.model.AlertMode
import com.nearwake.ports.persistence.TripLifecycleStore
import javax.inject.Inject

class UpdateTripAlertModeUseCase @Inject constructor(
    private val tripLifecycleStore: TripLifecycleStore,
) {
    suspend operator fun invoke(tripId: String, alertMode: AlertMode) {
        tripLifecycleStore.updateTripAlertMode(
            tripId = tripId,
            alertMode = alertMode,
        )
    }
}
