package com.nearwake.ports.persistence

import com.nearwake.domain.trip.model.AlertIntensity
import com.nearwake.domain.trip.model.AlertMode
import com.nearwake.domain.trip.model.AlertStage
import com.nearwake.domain.trip.model.AlertTriggerMode
import com.nearwake.domain.trip.model.Confidence
import com.nearwake.domain.trip.model.MonitoringMode
import com.nearwake.domain.trip.model.TripState
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

data class PersistedSavedPlace(
    val id: String,
    val name: String,
    val address: String,
    val lat: Double,
    val lng: Double,
)

data class PersistedTrip(
    val id: String,
    val destinationId: String,
    val alertLeadMinutes: Int,
    val alertTriggerMode: AlertTriggerMode,
    val alertDistanceMeters: Int,
    val alertIntensity: AlertIntensity,
    val alertMode: AlertMode,
    val createdAt: Instant,
    val completedAt: Instant?,
)

data class PersistedTripSession(
    val tripId: String,
    val state: TripState,
    val monitoringMode: MonitoringMode,
    val alertStage: AlertStage,
    val lastEtaMinutes: Int?,
    val confidence: Confidence,
)

data class PersistedHomeSnapshot(
    val trips: List<PersistedTrip>,
    val sessions: List<PersistedTripSession>,
    val savedPlaces: List<PersistedSavedPlace>,
)

data class SaveTripCommand(
    val id: String,
    val destinationId: String,
    val alertLeadMinutes: Int,
    val alertTriggerMode: AlertTriggerMode,
    val alertDistanceMeters: Int,
    val alertIntensity: AlertIntensity,
    val alertMode: AlertMode,
    val createdAt: Instant,
    val completedAt: Instant? = null,
)

data class SaveTripSessionCommand(
    val tripId: String,
    val state: TripState,
    val monitoringMode: MonitoringMode,
    val alertStage: AlertStage = AlertStage.MONITORING,
    val lastKnownLat: Double? = null,
    val lastKnownLng: Double? = null,
    val lastEtaMinutes: Int? = null,
    val confidence: Confidence,
    val geofenceIds: List<String>,
    val updatedAt: Instant,
)

interface TripLifecycleStore {
    fun observeHomeSnapshot(): Flow<PersistedHomeSnapshot>

    suspend fun getSavedPlace(placeId: String): PersistedSavedPlace?

    suspend fun getTrip(tripId: String): PersistedTrip?

    suspend fun updateTripAlertMode(tripId: String, alertMode: AlertMode)

    suspend fun clearTripSession(tripId: String)

    suspend fun markPlaceUsed(placeId: String, usedAt: Instant)

    suspend fun saveTrip(command: SaveTripCommand)

    suspend fun saveTripSession(command: SaveTripSessionCommand)
}
