package com.nearwake.domain.trip.model

import kotlinx.serialization.Serializable

@Serializable
enum class AlertIntensity {
    GENTLE,
    STANDARD,
    LOUD,
}
