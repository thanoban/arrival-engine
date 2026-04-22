package com.nearwake.domain.location.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class MotionState(
    val type: MotionType,
    val confidence: Int,
    val detectedAt: Instant,
)

@Serializable
enum class MotionType {
    IN_VEHICLE,
    ON_BICYCLE,
    ON_FOOT,
    STILL,
    UNKNOWN,
}
