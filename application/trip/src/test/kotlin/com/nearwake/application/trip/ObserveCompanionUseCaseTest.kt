package com.nearwake.application.trip

import com.nearwake.domain.trip.model.AlertIntensity
import com.nearwake.domain.trip.model.AlertMode
import com.nearwake.domain.trip.model.AlertTriggerMode
import com.nearwake.ports.persistence.PersistedHomeSnapshot
import com.nearwake.ports.persistence.PersistedSavedPlace
import com.nearwake.ports.persistence.PersistedTrip
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Instant
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ObserveCompanionUseCaseTest {
    @Test
    fun `builds arrival confirmation copy from persisted trip and place`() = runBlocking {
        val useCase = ObserveCompanionUseCase(
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
                            completedAt = Instant.parse("2026-05-05T03:10:00Z"),
                        ),
                    ),
                    sessions = emptyList(),
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

        val presentation = useCase("trip-1").first()

        assertEquals("trip-1", presentation.tripId)
        assertEquals("Arrival confirmed", presentation.title)
        assertEquals(presentation.messagePreview, presentation.smsPreview)
    }
}
