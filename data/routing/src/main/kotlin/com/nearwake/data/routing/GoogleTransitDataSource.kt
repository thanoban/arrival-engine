package com.nearwake.data.routing

import com.nearwake.domain.location.model.LatLng
import com.nearwake.domain.routing.RoutingDataSource
import com.nearwake.domain.routing.model.RouteSnapshot
import com.nearwake.domain.routing.model.Stop
import com.nearwake.domain.routing.model.TransferPoint
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.Locale

@Singleton
class GoogleTransitDataSource @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val json: Json,
    private val localRouteCache: LocalRouteCache,
) : RoutingDataSource {
    internal constructor(
        okHttpClient: OkHttpClient,
        json: Json,
        localRouteCache: LocalRouteCache,
        apiKey: String,
        directionsBaseUrl: HttpUrl,
    ) : this(okHttpClient, json, localRouteCache) {
        this.apiKey = apiKey
        this.directionsBaseUrl = directionsBaseUrl
    }

    private var apiKey: String = BuildConfig.MAPS_API_KEY
    private var directionsBaseUrl: HttpUrl = DEFAULT_DIRECTIONS_BASE_URL

    override suspend fun fetchRoute(
        origin: LatLng,
        destination: LatLng,
    ): Result<RouteSnapshot> {
        val key = apiKey
        if (key.isBlank()) {
            return Result.failure(
                IllegalStateException("Google transit routing requires MAPS_API_KEY to be configured."),
            )
        }

        val legResult = requestLeg(
            origin = origin,
            destination = destination,
            apiKey = key,
        )
        return legResult.map { leg ->
            val routeKey = routeKey(origin, destination)
            RouteSnapshot(
                tripId = routeKey,
                stops = leg.toStops(),
                transfers = leg.toTransferPoints(),
                totalDurationMinutes = leg.duration.toMinutes(),
                fetchedAt = Clock.System.now(),
                isStale = false,
            )
        }
    }

    override suspend fun refreshEta(
        tripId: String,
        currentLocation: LatLng,
    ): Result<Int> {
        val key = apiKey
        if (key.isBlank()) {
            return Result.failure(
                IllegalStateException("Google transit ETA refresh requires MAPS_API_KEY to be configured."),
            )
        }

        val cachedRoute = localRouteCache.get(tripId)
            ?: return Result.failure(
                IllegalStateException("No cached route snapshot is available for tripId=$tripId."),
            )
        val destinationStop = cachedRoute.stops.lastOrNull()
            ?: return Result.failure(
                IllegalStateException("Cached route for tripId=$tripId does not contain a destination stop."),
            )

        return requestLeg(
            origin = currentLocation,
            destination = LatLng(destinationStop.lat, destinationStop.lng),
            apiKey = key,
        ).map { leg ->
            leg.duration.toMinutes()
        }
    }

    private suspend fun requestLeg(
        origin: LatLng,
        destination: LatLng,
        apiKey: String,
    ): Result<GoogleLegDto> = withContext(Dispatchers.IO) {
        runCatching {
            val request = Request.Builder()
                .url(
                    directionsBaseUrl.newBuilder()
                        .addPathSegments("maps/api/directions/json")
                        .addQueryParameter("origin", origin.asQueryValue())
                        .addQueryParameter("destination", destination.asQueryValue())
                        .addQueryParameter("mode", "transit")
                        .addQueryParameter("departure_time", "now")
                        .addQueryParameter("transit_routing_preference", "fewer_transfers")
                        .addQueryParameter("key", apiKey)
                        .build(),
                )
                .get()
                .build()

            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IOException("Google Directions request failed with HTTP ${response.code}.")
                }

                val body = response.body?.string().orEmpty()
                val directions = json.decodeFromString<GoogleDirectionsResponse>(body)
                if (directions.status != "OK") {
                    val suffix = directions.errorMessage?.let { " $it" }.orEmpty()
                    throw IllegalStateException("Google Directions returned status ${directions.status}.$suffix".trim())
                }

                directions.routes.firstOrNull()?.legs?.firstOrNull()
                    ?: throw IllegalStateException("Google Directions returned no transit legs.")
            }
        }
    }

    private fun GoogleLegDto.toStops(): List<Stop> {
        val stops = mutableListOf<Stop>()
        transitSteps().forEach { step ->
            step.transitDetails?.departureStop?.toStop()?.let { stop ->
                if (stops.lastOrNull()?.matches(stop) != true) {
                    stops += stop.copy(order = stops.size + 1)
                }
            }
            step.transitDetails?.arrivalStop?.toStop()?.let { stop ->
                if (stops.lastOrNull()?.matches(stop) != true) {
                    stops += stop.copy(order = stops.size + 1)
                }
            }
        }
        return stops
    }

    private fun GoogleLegDto.toTransferPoints(): List<TransferPoint> {
        var elapsedSeconds = 0
        var transitCount = 0
        var previousTransitArrivalSeconds: Int? = null
        val transferPoints = mutableListOf<TransferPoint>()
        allSteps().forEach { step ->
            if (step.travelMode == "TRANSIT" && step.transitDetails != null) {
                if (transitCount > 0) {
                    val transferStop = step.transitDetails.departureStop?.toStop(order = transitCount + 1)
                    if (transferStop != null) {
                        transferPoints += TransferPoint(
                            stop = transferStop,
                            lineName = step.transitDetails.lineName(),
                            arrivalMinutes = previousTransitArrivalSeconds?.toMinutes()
                                ?: elapsedSeconds.toMinutes(),
                        )
                    }
                }
                transitCount += 1
            }
            elapsedSeconds += step.duration?.value ?: 0
            if (step.travelMode == "TRANSIT" && step.transitDetails != null) {
                previousTransitArrivalSeconds = elapsedSeconds
            }
        }
        return transferPoints
    }

    private fun GoogleLegDto.transitSteps(): List<GoogleStepDto> =
        allSteps().filter { candidate ->
            candidate.travelMode == "TRANSIT" && candidate.transitDetails != null
        }

    private fun GoogleLegDto.allSteps(): List<GoogleStepDto> =
        steps.flatMap { step -> step.flattened() }

    private fun GoogleStepDto.flattened(): List<GoogleStepDto> =
        listOf(this) + steps.flatMap { child -> child.flattened() }

    private fun GoogleTransitStopDto.toStop(order: Int = 1): Stop? {
        val locationValue = location ?: return null
        val stopName = name ?: return null
        return Stop(
            name = stopName,
            lat = locationValue.lat,
            lng = locationValue.lng,
            order = order,
        )
    }

    private fun Stop.matches(other: Stop): Boolean =
        name == other.name && lat == other.lat && lng == other.lng

    private fun GoogleTransitDetailsDto?.lineName(): String =
        this?.line?.shortName ?: this?.line?.name ?: "Transit line"

    private fun GoogleValueDto?.toMinutes(): Int =
        ((this?.value ?: 0) / 60.0).toInt().coerceAtLeast(1)

    private fun Int.toMinutes(): Int =
        (this / 60.0).toInt().coerceAtLeast(1)

    private fun LatLng.asQueryValue(): String =
        "${lat.formatCoordinate()},${lng.formatCoordinate()}"

    private fun Double.formatCoordinate(): String =
        String.format(Locale.US, "%.6f", this)

    private fun routeKey(origin: LatLng, destination: LatLng): String =
        buildString {
            append("google-transit:")
            append(origin.asQueryValue())
            append("->")
            append(destination.asQueryValue())
        }

    companion object {
        private val DEFAULT_DIRECTIONS_BASE_URL = "https://maps.googleapis.com/".toHttpUrl()
    }
}
