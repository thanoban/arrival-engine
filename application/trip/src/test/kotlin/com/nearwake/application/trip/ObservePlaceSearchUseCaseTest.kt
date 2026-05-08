package com.nearwake.application.trip

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Instant
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ObservePlaceSearchUseCaseTest {
    @Test
    fun maps_saved_places_into_presentations() = runBlocking {
        val store = FakeTripLifecycleStore().apply {
            savedPlaces += com.nearwake.ports.persistence.PersistedSavedPlace(
                id = "fort",
                name = "Colombo Fort",
                address = "Fort Railway Station",
                lat = 6.935,
                lng = 79.842,
                placeId = "fort",
                lastUsedAt = Instant.parse("2026-05-08T01:02:03Z"),
            )
        }

        val presentation = ObservePlaceSearchUseCase(store).invoke().first()

        assertEquals(1, presentation.size)
        assertEquals("Colombo Fort", presentation.first().name)
        assertEquals("Used 2026-05-08", presentation.first().lastUsedLabel)
    }
}
