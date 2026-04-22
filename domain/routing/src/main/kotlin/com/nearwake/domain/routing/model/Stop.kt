package com.nearwake.domain.routing.model

import kotlinx.serialization.Serializable

@Serializable
data class Stop(
    val name: String,
    val lat: Double,
    val lng: Double,
    val order: Int,
)
