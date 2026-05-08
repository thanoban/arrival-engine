package com.nearwake.application.trip

import com.nearwake.ports.persistence.TripLifecycleStore
import javax.inject.Inject

class ClearDiagnosticsEventsUseCase @Inject constructor(
    private val tripLifecycleStore: TripLifecycleStore,
) {
    suspend operator fun invoke() {
        tripLifecycleStore.clearDiagnosticsEvents()
    }
}
