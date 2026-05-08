package com.nearwake.application.trip

import com.nearwake.domain.trip.model.AlertIntensity
import com.nearwake.domain.trip.model.AlertMode
import com.nearwake.domain.trip.model.AlertTriggerMode
import com.nearwake.ports.persistence.PersistedDiagnosticsEvent
import com.nearwake.ports.persistence.PersistedHomeSnapshot
import com.nearwake.ports.persistence.PersistedSavedPlace
import com.nearwake.ports.persistence.PersistedTrip
import com.nearwake.ports.persistence.SaveSavedPlaceCommand
import com.nearwake.ports.persistence.SaveTripCommand
import com.nearwake.ports.persistence.SaveTripSessionCommand
import com.nearwake.ports.persistence.TripLifecycleStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.Instant

internal class FakeTripLifecycleStore : TripLifecycleStore {
    var completedTripId: String? = null
    var completedAt: Instant? = null
    val clearedTripIds = mutableListOf<String>()
    val diagnosticsEvents = mutableListOf<PersistedDiagnosticsEvent>()
    val savedPlaces = mutableListOf<PersistedSavedPlace>()

    override fun observeHomeSnapshot(): Flow<PersistedHomeSnapshot> =
        flowOf(
            PersistedHomeSnapshot(
                trips = emptyList(),
                sessions = emptyList(),
                savedPlaces = savedPlaces,
            ),
        )

    override fun observeSavedPlaces(): Flow<List<PersistedSavedPlace>> =
        flowOf(savedPlaces.toList())

    override fun observeRecentDiagnosticsEvents(limit: Int): Flow<List<PersistedDiagnosticsEvent>> =
        flowOf(diagnosticsEvents.take(limit))

    override suspend fun getSavedPlace(placeId: String): PersistedSavedPlace? =
        savedPlaces.firstOrNull { place -> place.id == placeId }

    override suspend fun getTrip(tripId: String): PersistedTrip? =
        PersistedTrip(
            id = tripId,
            destinationId = "place-1",
            alertLeadMinutes = 5,
            alertTriggerMode = AlertTriggerMode.TIME,
            alertDistanceMeters = 500,
            alertIntensity = AlertIntensity.STANDARD,
            alertMode = AlertMode.ACTIVE,
            createdAt = Instant.parse("2026-05-05T02:00:00Z"),
            completedAt = null,
        )

    override suspend fun updateTripAlertMode(tripId: String, alertMode: AlertMode) = Unit

    override suspend fun completeTrip(tripId: String, completedAt: Instant) {
        completedTripId = tripId
        this.completedAt = completedAt
    }

    override suspend fun clearTripSession(tripId: String) {
        clearedTripIds += tripId
    }

    override suspend fun clearDiagnosticsEvents() {
        diagnosticsEvents.clear()
    }

    override suspend fun markPlaceUsed(placeId: String, usedAt: Instant) {
        val existing = savedPlaces.firstOrNull { place -> place.id == placeId } ?: return
        savedPlaces.removeAll { place -> place.id == placeId }
        savedPlaces += existing.copy(lastUsedAt = usedAt)
    }

    override suspend fun saveSavedPlace(command: SaveSavedPlaceCommand) {
        savedPlaces.removeAll { place -> place.id == command.id }
        savedPlaces += PersistedSavedPlace(
            id = command.id,
            name = command.name,
            address = command.address,
            lat = command.lat,
            lng = command.lng,
            placeId = command.placeId,
            lastUsedAt = command.lastUsedAt,
        )
    }

    override suspend fun saveTrip(command: SaveTripCommand) = Unit

    override suspend fun saveTripSession(command: SaveTripSessionCommand) = Unit
}
