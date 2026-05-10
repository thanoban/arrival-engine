package com.nearwake.application.trip

import com.nearwake.application.monitoring.StopTripMonitoringUseCase
import com.nearwake.ports.analytics.NearWakeAnalytics
import com.nearwake.ports.persistence.TripLifecycleStore
import javax.inject.Inject
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

class CompleteTripUseCase @Inject constructor(
    private val tripLifecycleStore: TripLifecycleStore,
    private val stopTripMonitoring: StopTripMonitoringUseCase,
    private val analytics: NearWakeAnalytics,
) {
    suspend operator fun invoke(
        tripId: String,
        completedAt: Instant = Clock.System.now(),
    ) {
        val trip = tripLifecycleStore.getTrip(tripId)
        tripLifecycleStore.completeTrip(tripId = tripId, completedAt = completedAt)
        tripLifecycleStore.clearTripSession(tripId)
        stopTripMonitoring()
        analytics.trackTripCompleted(
            stageReached = "manual_completion",
            durationMinutes = trip?.createdAt?.let { createdAt ->
                ((completedAt.toEpochMilliseconds() - createdAt.toEpochMilliseconds()) / 60_000L).toInt()
                    .coerceAtLeast(0)
            },
            alertCount = null,
        )
    }
}
