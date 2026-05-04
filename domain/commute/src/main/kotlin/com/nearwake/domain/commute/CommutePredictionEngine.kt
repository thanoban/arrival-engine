package com.nearwake.domain.commute

import java.util.UUID
import kotlin.math.abs
import kotlin.math.roundToInt
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
                    EnrichedTrip(
                        trip = trip,
                        dayOfWeek = local.dayOfWeek.value,
                        departureMinuteOfDay = local.hour * 60 + local.minute,
                    )
                }

                clusterTrips(enriched)
                    .filter { cluster -> cluster.size >= MIN_TRIPS_REQUIRED }
                    .map { cluster ->
                        val avgMinuteOfDay = cluster.averageDepartureMinute()
                        val daysOfWeek = cluster.map { it.dayOfWeek }.toSet()
                        val avgDuration = cluster
                            .mapNotNull { it.trip.durationMinutes }
                            .takeIf { it.isNotEmpty() }
                            ?.average()
                            ?.roundToInt()
                            ?: DEFAULT_DURATION_MINUTES
                        CommutePrediction(
                            id = UUID.nameUUIDFromBytes(
                                "$destinationId:${daysOfWeek.sorted()}:$avgMinuteOfDay".toByteArray(),
                            ).toString(),
                            originId = null,
                            destinationId = destinationId,
                            destinationName = cluster.first().trip.destinationName,
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
        private const val CLUSTER_WINDOW_MINUTES = 30
    }

    private fun clusterTrips(trips: List<EnrichedTrip>): List<List<EnrichedTrip>> {
        if (trips.isEmpty()) return emptyList()

        val sortedTrips = trips.sortedBy { it.departureMinuteOfDay }
        val clusters = mutableListOf<MutableList<EnrichedTrip>>()

        sortedTrips.forEach { trip ->
            val currentCluster = clusters.lastOrNull()
            if (
                currentCluster == null ||
                abs(trip.departureMinuteOfDay - currentCluster.averageDepartureMinute()) > CLUSTER_WINDOW_MINUTES
            ) {
                clusters += mutableListOf(trip)
            } else {
                currentCluster += trip
            }
        }

        return clusters
    }

    private fun List<EnrichedTrip>.averageDepartureMinute(): Int =
        map { it.departureMinuteOfDay }.average().roundToInt()

    private data class EnrichedTrip(
        val trip: TripRecord,
        val dayOfWeek: Int,
        val departureMinuteOfDay: Int,
    )
}
