package com.nearwake.domain.commute

import java.util.UUID
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

data class TripRecord(
    val id: String,
    val destinationId: String,
    val destinationName: String,
    val createdAt: Instant,
    val durationMinutes: Int?,
)

class CommutePredictionEngine {
    fun predict(trips: List<TripRecord>): List<CommutePrediction> {
        if (trips.size < MIN_TRIPS_REQUIRED) return emptyList()

        val tz = TimeZone.currentSystemDefault()

        return trips
            .groupBy { it.destinationId }
            .flatMap { (destinationId, destinationTrips) ->
                if (destinationTrips.size < MIN_TRIPS_REQUIRED) return@flatMap emptyList()

                val enriched = destinationTrips.map { trip ->
                    val local = trip.createdAt.toLocalDateTime(tz)
                    Triple(trip, local.dayOfWeek.value, local.hour * 60 + local.minute)
                }

                enriched
                    .groupBy { (_, dayOfWeek, departureMinuteOfDay) ->
                        val roundedHour = (departureMinuteOfDay / 60)
                        "$dayOfWeek:$roundedHour"
                    }
                    .filter { (_, cluster) -> cluster.size >= MIN_TRIPS_REQUIRED }
                    .map { (_, cluster) ->
                        val avgMinuteOfDay = cluster.map { it.third }.average().toInt()
                        val daysOfWeek = cluster.map { it.second }.toSet()
                        val avgDuration = cluster
                            .mapNotNull { it.first.durationMinutes }
                            .takeIf { it.isNotEmpty() }
                            ?.average()
                            ?.toInt()
                            ?: DEFAULT_DURATION_MINUTES
                        CommutePrediction(
                            id = UUID.nameUUIDFromBytes("$destinationId:${daysOfWeek.sorted()}:${avgMinuteOfDay / 60}".toByteArray()).toString(),
                            originId = null,
                            destinationId = destinationId,
                            destinationName = cluster.first().first.destinationName,
                            daysOfWeek = daysOfWeek,
                            typicalDepartureHour = avgMinuteOfDay / 60,
                            typicalDepartureMinute = avgMinuteOfDay % 60,
                            avgDurationMinutes = avgDuration,
                            tripCount = cluster.size,
                        )
                    }
            }
            .sortedByDescending { it.tripCount }
    }

    companion object {
        private const val MIN_TRIPS_REQUIRED = 2
        private const val DEFAULT_DURATION_MINUTES = 30
    }
}
