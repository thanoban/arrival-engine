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
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ObserveWalkFinishUseCaseTest {
    @Test
    fun `builds final walk guidance from persisted destination and location`() = runBlocking {
        val useCase = ObserveWalkFinishUseCase(
            tripLifecycleStore = TestTripLifecycleStore(
                PersistedHomeSnapshot(
                    trips = listOf(
                        PersistedTrip(
                            id = "trip-1",
                            destinationId = "place-1",
                            alertLeadMinutes = 5,
                            alertTriggerMode = AlertTriggerMode.TIME,
                            alertDistanceMeters = 500,
                            alertIntensity = AlertIntensity.STANDARD,
                            alertMode = AlertMode.ACTIVE,
                            createdAt = Instant.parse("2026-05-05T01:00:00Z"),
                            completedAt = null,
                        ),
                    ),
                    sessions = listOf(
                        PersistedTripSession(
                            tripId = "trip-1",
                            state = TripState.Alerting,
                            monitoringMode = MonitoringMode.PRECISE_BURST,
                            alertStage = AlertStage.ARRIVAL,
                            lastKnownLat = 6.9340,
                            lastKnownLng = 79.8420,
                            lastEtaMinutes = 1,
                            confidence = Confidence.HIGH,
                            updatedAt = Instant.parse("2026-05-05T01:10:00Z"),
                        ),
                    ),
                    savedPlaces = listOf(
                        PersistedSavedPlace("place-1", "Colombo Fort", "Fort Station", 6.9344, 79.8428),
                    ),
                ),
            ),
        )

        val walkFinish = useCase("trip-1").first()

        assertEquals("Colombo Fort", walkFinish.destinationName)
        assertTrue(walkFinish.distanceLabel.contains("remaining"))
        assertTrue(walkFinish.headingLabel.startsWith("Head "))
    }
}
