package com.nearwake.domain.routing.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class RouteSnapshot(
    val tripId: String,
    val stops: List<Stop>,
    val transfers: List<TransferPoint>,
    val totalDurationMinutes: Int,
    val fetchedAt: Instant,
    val isStale: Boolean,
)
