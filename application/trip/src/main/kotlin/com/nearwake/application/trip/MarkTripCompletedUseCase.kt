package com.nearwake.application.trip

import com.nearwake.ports.analytics.NearWakeAnalytics
import com.nearwake.ports.persistence.TripLifecycleStore
import javax.inject.Inject
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

class MarkTripCompletedUseCase @Inject constructor(
    private val tripLifecycleStore: TripLifecycleStore,
    private val analytics: NearWakeAnalytics,
) {
    suspend operator fun invoke(
        tripId: String,
        completedAt: Instant = Clock.System.now(),
    ) {
        val trip = tripLifecycleStore.getTrip(tripId)
        tripLifecycleStore.completeTrip(tripId = tripId, completedAt = completedAt)
        analytics.trackTripCompleted(
            stageReached = "service_completion",
            durationMinutes = trip?.createdAt?.let { createdAt ->
                ((completedAt.toEpochMilliseconds() - createdAt.toEpochMilliseconds()) / 60_000L).toInt()
                    .coerceAtLeast(0)
            },
            alertCount = null,
        )
    }
}
