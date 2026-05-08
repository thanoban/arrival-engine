package com.nearwake.application.trip

import com.nearwake.domain.trip.model.AlertStage
import com.nearwake.domain.trip.model.Confidence
import com.nearwake.domain.trip.model.MonitoringMode
import com.nearwake.domain.trip.model.TripState
import com.nearwake.ports.persistence.PersistedDiagnosticsEvent
import com.nearwake.ports.persistence.PersistedHomeSnapshot
import com.nearwake.ports.persistence.PersistedTripSession
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Instant
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ObserveDiagnosticsUseCaseTest {
    @Test
    fun builds_diagnostics_presentation_from_store() = runBlocking {
        val store = TestTripLifecycleStore(
            snapshot = PersistedHomeSnapshot(
                trips = emptyList(),
                sessions = listOf(
                    PersistedTripSession(
                        tripId = "trip-1",
                        state = TripState.MonitoringApproach,
                        monitoringMode = MonitoringMode.BALANCED,
                        alertStage = AlertStage.APPROACH,
                        geofenceIds = listOf("approach-1", "destination-1"),
                        lastEtaMinutes = 6,
                        confidence = Confidence.HIGH,
                    ),
                ),
                savedPlaces = emptyList(),
            ),
            diagnosticsEvents = listOf(
                PersistedDiagnosticsEvent(
                    eventType = "monitoring_service_started",
                    tripId = "trip-1",
                    payloadJson = "{}",
                    recordedAt = Instant.parse("2026-05-08T01:02:03Z"),
                ),
            ),
        )

        val presentation = ObserveDiagnosticsUseCase(store).invoke().first()

        assertEquals("MonitoringApproach", presentation.stateLabel)
        assertEquals(listOf("approach-1", "destination-1"), presentation.registeredGeofences)
        assertEquals(1, presentation.recentEvents.size)
    }
}
