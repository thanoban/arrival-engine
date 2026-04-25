package com.nearwake.domain.routing.model

import kotlinx.serialization.Serializable

@Serializable
enum class RouteSignalQuality {
    HIGH,
    DEGRADED,
    OFFLINE,
}
