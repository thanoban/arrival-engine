package com.nearwake.domain.trip.model

import kotlinx.serialization.Serializable

@Serializable
enum class Confidence {
    HIGH,
    DEGRADED,
    OFFLINE,
}
