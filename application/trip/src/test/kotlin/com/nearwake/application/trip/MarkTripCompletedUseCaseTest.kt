package com.nearwake.application.trip

import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Instant
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class MarkTripCompletedUseCaseTest {
    @Test
    fun `marks trip complete without clearing active session`() = runBlocking {
        val tripStore = FakeTripLifecycleStore()
        val useCase = MarkTripCompletedUseCase(tripLifecycleStore = tripStore)
        val completedAt = Instant.parse("2026-05-06T03:00:00Z")

        useCase(tripId = "trip-1", completedAt = completedAt)

        assertEquals("trip-1", tripStore.completedTripId)
        assertEquals(completedAt, tripStore.completedAt)
        assertTrue(tripStore.clearedTripIds.isEmpty())
    }
}
