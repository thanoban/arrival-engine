package com.nearwake.feature.history

import com.nearwake.core.database.entity.TripEntity
import com.nearwake.core.database.entity.TripSessionEntity
import com.nearwake.core.database.entity.SavedPlaceEntity

internal fun buildTripCsv(
    trips: List<TripEntity>,
    places: Map<String, SavedPlaceEntity>,
    sessions: Map<String, TripSessionEntity>,
): String = buildString {
    appendLine("date,destination,address,alert_trigger_mode,alert_lead_minutes,alert_distance_meters,alert_intensity,status,monitoring_mode,confidence,eta_at_last_check_minutes,duration_minutes")
    trips.forEach { trip ->
        val place = places[trip.destinationId]
        val session = sessions[trip.id]
        val durationMinutes = if (trip.completedAt != null) {
            ((trip.completedAt!!.toEpochMilliseconds() - trip.createdAt.toEpochMilliseconds()) / 60_000).toInt()
        } else {
            -1
        }
        val row = listOf(
            trip.createdAt.toString().take(16).replace('T', ' '),
            place?.name.csvEscape(),
            place?.address.csvEscape(),
            trip.alertTriggerMode.name,
            trip.alertLeadMinutes.toString(),
            trip.alertDistanceMeters.toString(),
            trip.alertIntensity.name,
            when {
                trip.completedAt != null -> "Completed"
                session != null -> "Active"
                else -> "Ended early"
            },
            session?.monitoringMode?.name ?: "",
            session?.confidence?.name ?: "",
            session?.lastEtaMinutes?.toString() ?: "",
            durationMinutes.takeIf { it >= 0 }?.toString() ?: "",
        )
        appendLine(row.joinToString(","))
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
