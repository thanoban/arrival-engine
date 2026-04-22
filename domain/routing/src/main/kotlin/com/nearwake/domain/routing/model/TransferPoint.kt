package com.nearwake.domain.routing.model

import kotlinx.serialization.Serializable

@Serializable
data class TransferPoint(
    val stop: Stop,
    val lineName: String,
    val arrivalMinutes: Int,
)
