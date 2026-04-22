package com.nearwake.domain.trip.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class AlertDecision(
    val tripId: String,
    val reason: AlertReason,
    val confidence: Confidence,
    val decidedAt: Instant,
)

@Serializable
enum class AlertReason {
    ETA_THRESHOLD,
    GEOFENCE_ENTERED,
    OVERSHOOT,
}
