package com.nearwake.core.testing

import com.nearwake.domain.location.model.GeofenceSpec
import com.nearwake.domain.location.model.GeofenceType
import com.nearwake.domain.location.model.LatLng
import com.nearwake.domain.location.model.MotionState
import com.nearwake.domain.location.model.MotionType
import com.nearwake.domain.location.model.SavedPlace
import com.nearwake.domain.routing.model.RouteSnapshot
import com.nearwake.domain.routing.model.Stop
import com.nearwake.domain.routing.model.TransferPoint
import com.nearwake.domain.trip.model.AlertIntensity
import com.nearwake.domain.trip.model.Confidence
import com.nearwake.domain.trip.model.MonitoringMode
import com.nearwake.domain.trip.model.Trip
import com.nearwake.domain.trip.model.TripRule
import com.nearwake.domain.trip.model.TripSession
import com.nearwake.domain.trip.model.TripState
import kotlinx.datetime.Instant

object TripEngineTestFixtures {
    val fixedInstant: Instant = Instant.parse("2026-04-23T08:00:00Z")

    fun savedPlace(
        id: String = "central-station",
        name: String = "Central Station",
        address: String = "1 Station Plaza",
        lat: Double = 6.9271,
        lng: Double = 79.8612,
    ): SavedPlace = SavedPlace(
        id = id,
        name = name,
        address = address,
        lat = lat,
        lng = lng,
        placeId = "place-$id",
    )

    fun trip(
        id: String = "trip-123",
        destination: SavedPlace = savedPlace(),
        alertLeadMinutes: Int = 10,
        alertIntensity: AlertIntensity = AlertIntensity.STANDARD,
        routeSnapshot: RouteSnapshot? = null,
        createdAt: Instant = fixedInstant,
    ): Trip = Trip(
        id = id,
        destination = destination,
        alertLeadMinutes = alertLeadMinutes,
        alertIntensity = alertIntensity,
        routeSnapshot = routeSnapshot,
        createdAt = createdAt,
    )

    fun tripSession(
        tripId: String = "trip-123",
        state: TripState = TripState.Armed,
        monitoringMode: MonitoringMode = MonitoringMode.GEOFENCE_ONLY,
        confidence: Confidence = Confidence.HIGH,
        geofenceIds: List<String> = listOf("approach-$tripId", "destination-$tripId"),
        updatedAt: Instant = fixedInstant,
        lastKnownLocation: LatLng? = null,
        lastEtaMinutes: Int? = 12,
    ): TripSession = TripSession(
        tripId = tripId,
        state = state,
        monitoringMode = monitoringMode,
        lastKnownLat = lastKnownLocation?.lat,
        lastKnownLng = lastKnownLocation?.lng,
        lastEtaMinutes = lastEtaMinutes,
        confidence = confidence,
        geofenceIds = geofenceIds,
        updatedAt = updatedAt,
    )

    fun tripRule(
        alertLeadMinutes: Int = 10,
        approachRadiusMeters: Float = 1_500f,
        destinationRadiusMeters: Float = 300f,
        offlineBiasPercent: Int = 20,
        preciseBurstMaxSeconds: Int = 90,
        noMotionTimeoutMinutes: Int = 15,
    ): TripRule = TripRule(
        alertLeadMinutes = alertLeadMinutes,
        approachRadiusMeters = approachRadiusMeters,
        destinationRadiusMeters = destinationRadiusMeters,
        offlineBiasPercent = offlineBiasPercent,
        preciseBurstMaxSeconds = preciseBurstMaxSeconds,
        noMotionTimeoutMinutes = noMotionTimeoutMinutes,
    )

    fun routeSnapshot(
        tripId: String = "trip-123",
        fetchedAt: Instant = fixedInstant,
        isStale: Boolean = false,
    ): RouteSnapshot = RouteSnapshot(
        tripId = tripId,
        stops = listOf(
            stop(name = "Central Station", order = 1),
            stop(name = "Airport Terminal 2", order = 2, lat = 7.1808, lng = 79.8841),
        ),
        transfers = listOf(
            TransferPoint(
                stop = stop(name = "Central Station", order = 1),
                lineName = "Blue Line",
                arrivalMinutes = 12,
            ),
        ),
        totalDurationMinutes = 42,
        fetchedAt = fetchedAt,
        isStale = isStale,
    )

    fun stop(
        name: String = "Central Station",
        lat: Double = 6.9271,
        lng: Double = 79.8612,
        order: Int = 1,
    ): Stop = Stop(
        name = name,
        lat = lat,
        lng = lng,
        order = order,
    )

    fun geofenceSpec(
        id: String = "approach-central-station",
        center: LatLng = latLng(),
        radiusMeters: Float = 1_500f,
        type: GeofenceType = GeofenceType.APPROACH,
    ): GeofenceSpec = GeofenceSpec(
        id = id,
        center = center,
        radiusMeters = radiusMeters,
        type = type,
    )

    fun motionState(
        type: MotionType = MotionType.IN_VEHICLE,
        confidence: Int = 90,
        detectedAt: Instant = fixedInstant,
    ): MotionState = MotionState(
        type = type,
        confidence = confidence,
        detectedAt = detectedAt,
    )

    fun latLng(
        lat: Double = 6.9271,
        lng: Double = 79.8612,
    ): LatLng = LatLng(lat = lat, lng = lng)
}
