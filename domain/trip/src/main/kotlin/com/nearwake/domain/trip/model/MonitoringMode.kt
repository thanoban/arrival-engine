package com.nearwake.domain.trip.model

import kotlinx.serialization.Serializable

@Serializable
enum class MonitoringMode {
    GEOFENCE_ONLY,
    BALANCED,
    PRECISE_BURST,
}
