package com.nearwake.domain.location.model

import kotlinx.serialization.Serializable

@Serializable
data class SavedPlace(
    val id: String,
    val name: String,
    val address: String,
    val lat: Double,
    val lng: Double,
    val placeId: String? = null,
) {
    val coordinates: LatLng
        get() = LatLng(lat = lat, lng = lng)
}
