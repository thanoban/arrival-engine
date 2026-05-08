package com.nearwake.application.trip

import com.nearwake.ports.persistence.TripLifecycleStore
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.Instant

data class DiagnosticsEventPresentation(
    val eventType: String,
    val tripId: String?,
    val payloadJson: String,
    val recordedAt: Instant,
)

data class DiagnosticsPresentation(
    val stateLabel: String,
    val registeredGeofences: List<String>,
    val recentEvents: List<DiagnosticsEventPresentation>,
)

class ObserveDiagnosticsUseCase @Inject constructor(
    private val tripLifecycleStore: TripLifecycleStore,
) {
    operator fun invoke(limit: Int = 20): Flow<DiagnosticsPresentation> =
        combine(
            tripLifecycleStore.observeHomeSnapshot(),
            tripLifecycleStore.observeRecentDiagnosticsEvents(limit),
        ) { snapshot, recentEvents ->
            val session = snapshot.sessions.firstOrNull()
            DiagnosticsPresentation(
                stateLabel = session?.state?.name ?: "No active trip",
                registeredGeofences = session?.geofenceIds.orEmpty(),
                recentEvents = recentEvents.map { event ->
                    DiagnosticsEventPresentation(
                        eventType = event.eventType,
                        tripId = event.tripId,
                        payloadJson = event.payloadJson,
                        recordedAt = event.recordedAt,
                    )
                },
            )
        }
}
