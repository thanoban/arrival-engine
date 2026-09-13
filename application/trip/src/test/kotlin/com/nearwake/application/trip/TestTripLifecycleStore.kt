package com.nearwake.application.trip

import com.nearwake.ports.persistence.PersistedDiagnosticsEvent
import com.nearwake.domain.trip.model.AlertMode
import com.nearwake.ports.persistence.PersistedHomeSnapshot
import com.nearwake.ports.persistence.PersistedSavedPlace
import com.nearwake.ports.persistence.PersistedTrip
import com.nearwake.ports.persistence.SaveSavedPlaceCommand
import com.nearwake.ports.persistence.SaveTripCommand
import com.nearwake.ports.persistence.SaveTripSessionCommand
import com.nearwake.ports.persistence.TripLifecycleStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.Instant

internal class TestTripLifecycleStore(
    snapshot: PersistedHomeSnapshot,
    diagnosticsEvents: List<PersistedDiagnosticsEvent> = emptyList(),
) : TripLifecycleStore {
    private val state = MutableStateFlow(snapshot)
    private val diagnosticsState = MutableStateFlow(diagnosticsEvents)
    var savedTripSession: SaveTripSessionCommand? = null
        private set

    override fun observeHomeSnapshot(): Flow<PersistedHomeSnapshot> = state

    override fun observeSavedPlaces(): Flow<List<PersistedSavedPlace>> =
        MutableStateFlow(state.value.savedPlaces)

    override fun observeRecentDiagnosticsEvents(limit: Int): Flow<List<PersistedDiagnosticsEvent>> =
        diagnosticsState

    override suspend fun getSavedPlace(placeId: String): PersistedSavedPlace? =
        state.value.savedPlaces.firstOrNull { place -> place.id == placeId }

    override suspend fun getTrip(tripId: String): PersistedTrip? =
        state.value.trips.firstOrNull { trip -> trip.id == tripId }

    override suspend fun updateTripAlertMode(tripId: String, alertMode: AlertMode) = Unit

    override suspend fun completeTrip(tripId: String, completedAt: Instant) = Unit

    override suspend fun clearTripSession(tripId: String) = Unit

    override suspend fun clearDiagnosticsEvents() {
        diagnosticsState.value = emptyList()
    }

    override suspend fun markPlaceUsed(placeId: String, usedAt: Instant) = Unit

    override suspend fun saveSavedPlace(command: SaveSavedPlaceCommand) = Unit

    override suspend fun saveTrip(command: SaveTripCommand) = Unit

    override suspend fun saveTripSession(command: SaveTripSessionCommand) {
        savedTripSession = command
    }
}
