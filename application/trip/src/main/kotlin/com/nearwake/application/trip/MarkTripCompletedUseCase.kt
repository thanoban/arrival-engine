package com.nearwake.application.trip

import com.nearwake.ports.persistence.TripLifecycleStore
import javax.inject.Inject
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

class MarkTripCompletedUseCase @Inject constructor(
    private val tripLifecycleStore: TripLifecycleStore,
) {
    suspend operator fun invoke(
        tripId: String,
        completedAt: Instant = Clock.System.now(),
    ) {
        tripLifecycleStore.completeTrip(tripId = tripId, completedAt = completedAt)
    }
}
