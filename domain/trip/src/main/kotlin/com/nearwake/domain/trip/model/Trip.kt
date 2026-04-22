package com.nearwake.domain.trip.model

import com.nearwake.domain.location.model.SavedPlace
import com.nearwake.domain.routing.model.RouteSnapshot
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Trip(
    val id: String,
    val destination: SavedPlace,
    val alertLeadMinutes: Int,
    val alertIntensity: AlertIntensity,
    val routeSnapshot: RouteSnapshot? = null,
    val createdAt: Instant,
    val completedAt: Instant? = null,
)
