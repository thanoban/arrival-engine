package com.nearwake.data.location

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SamplePlaceSearchRepositoryTest {
    private val repository = SamplePlaceSearchRepository()

    @Test
    fun searchPlacesFiltersByName() = runTest {
        val result = repository.searchPlaces("airport").getOrThrow()

        assertEquals(1, result.size)
        assertEquals("Airport Terminal 2", result.first().name)
    }

    @Test
    fun resolvePlaceReturnsCoordinatesForSamplePlace() = runTest {
        val result = repository.resolvePlace("central-station").getOrThrow()

        assertEquals("Central Station", result.name)
        assertEquals(6.9271, result.latLng.lat)
        assertEquals(79.8612, result.latLng.lng)
    }

    @Test
    fun resolvePlaceFailsForUnknownPlace() = runTest {
        val result = repository.resolvePlace("missing")

        assertTrue(result.isFailure)
    }
}
