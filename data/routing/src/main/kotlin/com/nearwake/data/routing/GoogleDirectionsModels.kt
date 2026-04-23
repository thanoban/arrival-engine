package com.nearwake.data.routing

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GoogleDirectionsResponse(
    val status: String,
    @SerialName("error_message")
    val errorMessage: String? = null,
    val routes: List<GoogleRouteDto> = emptyList(),
)

@Serializable
data class GoogleRouteDto(
    val legs: List<GoogleLegDto> = emptyList(),
)

@Serializable
data class GoogleLegDto(
    val duration: GoogleValueDto? = null,
    val steps: List<GoogleStepDto> = emptyList(),
)

@Serializable
data class GoogleStepDto(
    @SerialName("travel_mode")
    val travelMode: String? = null,
    val duration: GoogleValueDto? = null,
    val steps: List<GoogleStepDto> = emptyList(),
    @SerialName("transit_details")
    val transitDetails: GoogleTransitDetailsDto? = null,
)

@Serializable
data class GoogleTransitDetailsDto(
    @SerialName("departure_stop")
    val departureStop: GoogleTransitStopDto? = null,
    @SerialName("arrival_stop")
    val arrivalStop: GoogleTransitStopDto? = null,
    val line: GoogleTransitLineDto? = null,
    @SerialName("num_stops")
    val numStops: Int? = null,
)

@Serializable
data class GoogleTransitStopDto(
    val name: String? = null,
    val location: GoogleLatLngDto? = null,
)

@Serializable
data class GoogleTransitLineDto(
    @SerialName("short_name")
    val shortName: String? = null,
    val name: String? = null,
)

@Serializable
data class GoogleLatLngDto(
    val lat: Double,
    val lng: Double,
)

@Serializable
data class GoogleValueDto(
    val value: Int? = null,
)
