package com.nearwake.data.location.receivers

data class GeofenceTransitionEvent(
    val geofenceIds: List<String>,
    val transitionType: Int,
    val errorCode: Int? = null,
)
