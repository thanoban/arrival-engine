package com.nearwake.application.trip

import com.nearwake.ports.persistence.PersistedSavedPlace
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Instant
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class MarkSavedPlaceUsedUseCaseTest {
    @Test
    fun marks_saved_place_as_used() = runBlocking {
        val store = FakeTripLifecycleStore().apply {
            savedPlaces += PersistedSavedPlace(
                id = "place-1",
                name = "Fort",
                address = "Colombo Fort",
                lat = 6.934,
                lng = 79.842,
                lastUsedAt = Instant.parse("2026-05-01T00:00:00Z"),
            )
        }

        MarkSavedPlaceUsedUseCase(store)("place-1")

        assertNotNull(store.getSavedPlace("place-1")?.lastUsedAt)
    }
}
