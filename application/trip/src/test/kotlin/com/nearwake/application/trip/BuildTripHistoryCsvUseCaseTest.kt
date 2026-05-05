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
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Instant
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class BuildTripHistoryCsvUseCaseTest {
    @Test
    fun `exports persisted history as csv`() = runBlocking {
        val useCase = BuildTripHistoryCsvUseCase(
            tripLifecycleStore = TestTripLifecycleStore(
                PersistedHomeSnapshot(
                    trips = listOf(
                        PersistedTrip(
                            id = "trip-1",
                            destinationId = "place-1",
                            alertLeadMinutes = 5,
                            alertTriggerMode = AlertTriggerMode.BOTH,
                            alertDistanceMeters = 750,
                            alertIntensity = AlertIntensity.LOUD,
                            alertMode = AlertMode.SLEEP,
                            createdAt = Instant.parse("2026-05-04T05:30:00Z"),
                            completedAt = Instant.parse("2026-05-04T06:00:00Z"),
                        ),
                    ),
                    sessions = listOf(
                        PersistedTripSession(
                            tripId = "trip-1",
                            state = TripState.Completed,
                            monitoringMode = MonitoringMode.BALANCED,
                            alertStage = AlertStage.ARRIVAL,
                            lastEtaMinutes = 2,
                            confidence = Confidence.HIGH,
                        ),
                    ),
                    savedPlaces = listOf(
                        PersistedSavedPlace(
                            id = "place-1",
                            name = "Colombo Fort",
                            address = "Fort Station",
                            lat = 6.9344,
                            lng = 79.8428,
                        ),
                    ),
                ),
            ),
        )

        val csv = useCase()

        assertTrue(csv.contains("date,destination,address,alert_trigger_mode"))
        assertTrue(csv.contains("Colombo Fort,Fort Station,BOTH,5,750,LOUD,Completed,BALANCED,HIGH,2,30"))
    }
}
