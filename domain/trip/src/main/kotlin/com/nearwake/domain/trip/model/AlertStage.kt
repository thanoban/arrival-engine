package com.nearwake.domain.trip.model

import kotlinx.serialization.Serializable

@Serializable
enum class AlertStage {
    MONITORING,
    APPROACH,
    IMMINENT,
    ARRIVAL,
    RECOVERY,
}
