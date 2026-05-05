package com.nearwake.application.trip

import com.nearwake.ports.persistence.PersistedSavedPlace
import com.nearwake.ports.persistence.PersistedTrip
import com.nearwake.ports.persistence.PersistedTripSession
import com.nearwake.ports.persistence.TripLifecycleStore
import javax.inject.Inject
import kotlinx.coroutines.flow.first

class BuildTripHistoryCsvUseCase @Inject constructor(
    private val tripLifecycleStore: TripLifecycleStore,
) {
    suspend operator fun invoke(): String {
        val snapshot = tripLifecycleStore.observeHomeSnapshot().first()
        return buildString {
            appendLine("date,destination,address,alert_trigger_mode,alert_lead_minutes,alert_distance_meters,alert_intensity,status,monitoring_mode,confidence,eta_at_last_check_minutes,duration_minutes")
            val placesById = snapshot.savedPlaces.associateBy { place -> place.id }
            val sessionsByTripId = snapshot.sessions.associateBy { session -> session.tripId }
            snapshot.trips.forEach { trip ->
                appendLine(
                    buildCsvRow(
                        trip = trip,
                        place = placesById[trip.destinationId],
                        session = sessionsByTripId[trip.id],
                    ),
                )
            }
        }
    }

    private fun buildCsvRow(
        trip: PersistedTrip,
        place: PersistedSavedPlace?,
        session: PersistedTripSession?,
    ): String {
        val durationMinutes = trip.completedAt?.let { completedAt ->
            ((completedAt.toEpochMilliseconds() - trip.createdAt.toEpochMilliseconds()) / 60_000).toInt()
        }
        return listOf(
            trip.createdAt.toString().take(16).replace('T', ' '),
            place?.name.csvEscape(),
            place?.address.csvEscape(),
            trip.alertTriggerMode.name,
            trip.alertLeadMinutes.toString(),
            trip.alertDistanceMeters.toString(),
            trip.alertIntensity.name,
            session.toStatusLabel(trip),
            session?.monitoringMode?.name ?: "",
            session?.confidence?.name ?: "",
            session?.lastEtaMinutes?.toString() ?: "",
            durationMinutes?.toString().orEmpty(),
        ).joinToString(",")
    }
}

private fun String?.csvEscape(): String {
    if (this == null) return ""
    return if (contains(',') || contains('"') || contains('\n')) {
        "\"${replace("\"", "\"\"")}\""
    } else {
        this
    }
}
