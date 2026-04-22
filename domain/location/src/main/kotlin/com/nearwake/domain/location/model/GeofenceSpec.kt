package com.nearwake.domain.location.model

import kotlinx.serialization.Serializable

@Serializable
data class GeofenceSpec(
    val id: String,
    val center: LatLng,
    val radiusMeters: Float,
    val type: GeofenceType,
)

@Serializable
enum class GeofenceType {
    APPROACH,
    DESTINATION,
}
