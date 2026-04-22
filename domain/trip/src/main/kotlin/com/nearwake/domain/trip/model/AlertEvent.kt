package com.nearwake.domain.trip.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class AlertEvent(
    val id: String,
    val tripId: String,
    val firedAt: Instant,
    val dismissedAt: Instant? = null,
    val type: AlertType,
)

@Serializable
enum class AlertType {
    APPROACH,
    ARRIVAL,
    RECOVERY,
}
