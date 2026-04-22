package com.nearwake.domain.trip.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class TripSession(
    val tripId: String,
    val state: TripState,
    val monitoringMode: MonitoringMode,
    val lastKnownLat: Double? = null,
    val lastKnownLng: Double? = null,
    val lastEtaMinutes: Int? = null,
    val confidence: Confidence = Confidence.HIGH,
    val geofenceIds: List<String> = emptyList(),
    val updatedAt: Instant,
)

val TripSession.isActive: Boolean
    get() = !state.isTerminal
