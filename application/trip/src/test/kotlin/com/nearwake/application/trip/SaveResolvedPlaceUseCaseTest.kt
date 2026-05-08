package com.nearwake.application.trip

import com.nearwake.domain.location.model.LatLng
import com.nearwake.domain.location.model.ResolvedPlace
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class SaveResolvedPlaceUseCaseTest {
    @Test
    fun persists_resolved_place_to_store() = runBlocking {
        val store = FakeTripLifecycleStore()
        val useCase = SaveResolvedPlaceUseCase(store)

        useCase(
            ResolvedPlace(
                id = "place-1",
                name = "Maharagama",
                address = "Maharagama, Sri Lanka",
                latLng = LatLng(6.8488, 79.9265),
            ),
        )

        val saved = store.getSavedPlace("place-1")
        assertNotNull(saved)
        assertEquals("Maharagama", saved?.name)
        assertEquals("Maharagama, Sri Lanka", saved?.address)
        assertEquals("place-1", saved?.placeId)
    }
}
