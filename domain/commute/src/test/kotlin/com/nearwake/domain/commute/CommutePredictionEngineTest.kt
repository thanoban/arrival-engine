package com.nearwake.domain.commute

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CommutePredictionEngineTest {
    private val engine = CommutePredictionEngine()
    private val timeZone = TimeZone.currentSystemDefault()

    @Test
    fun `clusters nearby departures across weekdays into one commute prediction`() {
        val trips = listOf(
            trip(
                id = "trip-1",
                destinationId = "fort",
                destinationName = "Colombo Fort",
                dateTime = LocalDateTime(2025, 5, 5, 10, 59),
                durationMinutes = 40,
            ),
            trip(
                id = "trip-2",
                destinationId = "fort",
                destinationName = "Colombo Fort",
                dateTime = LocalDateTime(2025, 5, 6, 11, 0),
                durationMinutes = 50,
            ),
        )

        val predictions = engine.predict(trips)

        assertEquals(1, predictions.size)
        assertEquals(setOf(1, 2), predictions.single().daysOfWeek)
        assertEquals(11, predictions.single().typicalDepartureHour)
        assertEquals(0, predictions.single().typicalDepartureMinute)
        assertEquals(45, predictions.single().avgDurationMinutes)
        assertEquals(2, predictions.single().tripCount)
    }

    @Test
    fun `keeps separate commute windows apart for the same destination`() {
        val trips = listOf(
            trip("trip-1", "office", "Office", LocalDateTime(2025, 5, 5, 8, 10), 30),
            trip("trip-2", "office", "Office", LocalDateTime(2025, 5, 6, 8, 25), 32),
            trip("trip-3", "office", "Office", LocalDateTime(2025, 5, 5, 18, 5), 38),
            trip("trip-4", "office", "Office", LocalDateTime(2025, 5, 6, 18, 15), 40),
        )

        val predictions = engine.predict(trips)

        assertEquals(2, predictions.size)
        assertEquals(listOf(8, 18), predictions.map { it.typicalDepartureHour }.sorted())
        assertTrue(predictions.all { it.tripCount == 2 })
        assertTrue(predictions.all { it.daysOfWeek == setOf(1, 2) })
    }

    private fun trip(
        id: String,
        destinationId: String,
        destinationName: String,
        dateTime: LocalDateTime,
        durationMinutes: Int?,
    ): TripRecord =
        TripRecord(
            id = id,
            destinationId = destinationId,
            destinationName = destinationName,
            createdAt = dateTime.toInstant(timeZone),
            durationMinutes = durationMinutes,
        )
}
