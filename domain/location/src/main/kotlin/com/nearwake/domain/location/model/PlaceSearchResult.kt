package com.nearwake.domain.location.model

data class PlaceSearchResult(
    val id: String,
    val name: String,
    val address: String,
)

data class ResolvedPlace(
    val id: String,
    val name: String,
    val address: String,
    val latLng: LatLng,
)
