package com.nearwake.application.trip

import com.nearwake.domain.trip.model.AlertStage
import com.nearwake.domain.trip.model.Confidence
import com.nearwake.domain.trip.model.MonitoringMode
import com.nearwake.domain.trip.model.TripState
import com.nearwake.ports.persistence.PersistedHomeSnapshot
import com.nearwake.ports.persistence.PersistedTripSession
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Instant
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class ObserveAlertUseCaseTest {
    @Test
    fun `missing eta remains unavailable instead of becoming a placeholder`() = runBlocking {
        val store = TestTripLifecycleStore(
            PersistedHomeSnapshot(
                trips = emptyList(),
                sessions = emptyList(),
                savedPlaces = emptyList(),
            ),
        )

        val presentation = ObserveAlertUseCase(store)("trip-1").first()

        assertNull(presentation.etaMinutes)
    }

    @Test
    fun `persisted eta is exposed as a typed value`() = runBlocking {
        val store = TestTripLifecycleStore(
            PersistedHomeSnapshot(
                trips = emptyList(),
                sessions = listOf(
                    PersistedTripSession(
                        tripId = "trip-1",
                        state = TripState.Alerting,
                        monitoringMode = MonitoringMode.GEOFENCE_ONLY,
                        alertStage = AlertStage.ARRIVAL,
                        lastKnownLat = null,
                        lastKnownLng = null,
                        lastEtaMinutes = 2,
                        confidence = Confidence.HIGH,
                        updatedAt = Instant.parse("2026-09-13T00:00:00Z"),
                    ),
                ),
                savedPlaces = emptyList(),
            ),
        )

        val presentation = ObserveAlertUseCase(store)("trip-1").first()

        assertEquals(2, presentation.etaMinutes)
    }
}
