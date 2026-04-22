package com.nearwake.domain.location.model

import kotlinx.serialization.Serializable

@Serializable
data class LatLng(
    val lat: Double,
    val lng: Double,
)
