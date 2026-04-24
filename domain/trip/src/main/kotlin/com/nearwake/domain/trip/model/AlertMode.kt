package com.nearwake.domain.trip.model

import kotlinx.serialization.Serializable

@Serializable
enum class AlertMode {
    ACTIVE,
    SLEEP,
}
