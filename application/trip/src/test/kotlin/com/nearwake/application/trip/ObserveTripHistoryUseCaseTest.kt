package com.nearwake.application.trip

import com.nearwake.domain.trip.model.AlertIntensity
import com.nearwake.domain.trip.model.AlertMode
import com.nearwake.domain.trip.model.AlertStage
import com.nearwake.domain.trip.model.AlertTriggerMode
import com.nearwake.domain.trip.model.Confidence
import com.nearwake.domain.trip.model.MonitoringMode
import com.nearwake.domain.trip.model.TripState
import com.nearwake.ports.persistence.PersistedHomeSnapshot
import com.nearwake.ports.persistence.PersistedSavedPlace
import com.nearwake.ports.persistence.PersistedTrip
import com.nearwake.ports.persistence.PersistedTripSession
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Instant
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ObserveTripHistoryUseCaseTest {
    @Test
    fun `marks active and completed trips from persisted snapshot`() = runBlocking {
        val useCase = ObserveTripHistoryUseCase(
            tripLifecycleStore = TestTripLifecycleStore(
                PersistedHomeSnapshot(
                    trips = listOf(
                        PersistedTrip(
                            id = "active-trip",
                            destinationId = "place-1",
                            alertLeadMinutes = 5,
                            alertTriggerMode = AlertTriggerMode.TIME,
                            alertDistanceMeters = 500,
                            alertIntensity = AlertIntensity.STANDARD,
                            alertMode = AlertMode.ACTIVE,
                            createdAt = Instant.parse("2026-05-04T05:30:00Z"),
                            completedAt = null,
                        ),
                        PersistedTrip(
                            id = "done-trip",
                            destinationId = "place-2",
                            alertLeadMinutes = 0,
                            alertTriggerMode = AlertTriggerMode.DISTANCE,
                            alertDistanceMeters = 300,
                            alertIntensity = AlertIntensity.GENTLE,
                            alertMode = AlertMode.ACTIVE,
                            createdAt = Instant.parse("2026-05-03T05:30:00Z"),
                            completedAt = Instant.parse("2026-05-03T06:00:00Z"),
                        ),
                    ),
                    sessions = listOf(
                        PersistedTripSession(
                            tripId = "active-trip",
                            state = TripState.MonitoringApproach,
                            monitoringMode = MonitoringMode.BALANCED,
                            alertStage = AlertStage.APPROACH,
                            lastEtaMinutes = 6,
                            confidence = Confidence.DEGRADED,
                        ),
                    ),
                    savedPlaces = listOf(
                        PersistedSavedPlace("place-1", "Colombo Fort", "Fort Station", 6.93, 79.84),
                        PersistedSavedPlace("place-2", "Maradana", "Railway Station", 6.92, 79.86),
                    ),
                ),
            ),
        )

        val history = useCase().first()

        assertEquals(2, history.trips.size)
        assertEquals("Monitoring now", history.trips.first().statusLabel)
        assertEquals("Completed", history.trips.last().statusLabel)
    }
}
