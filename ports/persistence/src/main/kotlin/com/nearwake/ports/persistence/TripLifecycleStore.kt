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
    val placeId: String? = null,
    val lastUsedAt: Instant? = null,
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
    val geofenceIds: List<String> = emptyList(),
    val lastKnownLat: Double? = null,
    val lastKnownLng: Double? = null,
    val lastEtaMinutes: Int?,
    val confidence: Confidence,
    val updatedAt: Instant? = null,
)

data class PersistedDiagnosticsEvent(
    val eventType: String,
    val tripId: String?,
    val payloadJson: String,
    val recordedAt: Instant,
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

data class SaveSavedPlaceCommand(
    val id: String,
    val name: String,
    val address: String,
    val lat: Double,
    val lng: Double,
    val placeId: String? = null,
    val lastUsedAt: Instant? = null,
)

interface TripLifecycleStore {
    fun observeHomeSnapshot(): Flow<PersistedHomeSnapshot>

    fun observeSavedPlaces(): Flow<List<PersistedSavedPlace>>

    fun observeRecentDiagnosticsEvents(limit: Int = 200): Flow<List<PersistedDiagnosticsEvent>>

    suspend fun getSavedPlace(placeId: String): PersistedSavedPlace?

    suspend fun getTrip(tripId: String): PersistedTrip?

    suspend fun updateTripAlertMode(tripId: String, alertMode: AlertMode)

    suspend fun completeTrip(tripId: String, completedAt: Instant)

    suspend fun clearTripSession(tripId: String)

    suspend fun clearDiagnosticsEvents()

    suspend fun markPlaceUsed(placeId: String, usedAt: Instant)

    suspend fun saveSavedPlace(command: SaveSavedPlaceCommand)

    suspend fun saveTrip(command: SaveTripCommand)

    suspend fun saveTripSession(command: SaveTripSessionCommand)
}
