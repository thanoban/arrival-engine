package com.nearwake.core.database

import com.nearwake.core.database.dao.SavedPlaceDao
import com.nearwake.core.database.dao.TripDao
import com.nearwake.core.database.dao.TripSessionDao
import com.nearwake.core.database.entity.TripEntity
import com.nearwake.core.database.entity.TripSessionEntity
import com.nearwake.ports.persistence.PersistedHomeSnapshot
import com.nearwake.ports.persistence.PersistedSavedPlace
import com.nearwake.ports.persistence.PersistedTrip
import com.nearwake.ports.persistence.PersistedTripSession
import com.nearwake.ports.persistence.SaveTripCommand
import com.nearwake.ports.persistence.SaveTripSessionCommand
import com.nearwake.ports.persistence.TripLifecycleStore
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

@Singleton
class TripLifecycleStoreImpl @Inject constructor(
    private val savedPlaceDao: SavedPlaceDao,
    private val tripDao: TripDao,
    private val tripSessionDao: TripSessionDao,
) : TripLifecycleStore {
    override fun observeHomeSnapshot(): Flow<PersistedHomeSnapshot> =
        combine(
            tripDao.observeTrips(),
            tripSessionDao.observeTripSessions(),
            savedPlaceDao.observeSavedPlaces(),
        ) { trips, sessions, places ->
            PersistedHomeSnapshot(
                trips = trips.map { trip -> trip.toPersistedTrip() },
                sessions = sessions.map { session -> session.toPersistedTripSession() },
                savedPlaces = places.map { place -> place.toPersistedSavedPlace() },
            )
        }

    override suspend fun getSavedPlace(placeId: String): PersistedSavedPlace? =
        savedPlaceDao.getSavedPlaceById(placeId)?.toPersistedSavedPlace()

    override suspend fun getTrip(tripId: String): PersistedTrip? =
        tripDao.getTripById(tripId)?.toPersistedTrip()

    override suspend fun updateTripAlertMode(tripId: String, alertMode: com.nearwake.domain.trip.model.AlertMode) {
        tripDao.getTripById(tripId)?.let { trip ->
            tripDao.upsertTrip(trip.copy(alertMode = alertMode))
        }
    }

    override suspend fun clearTripSession(tripId: String) {
        tripSessionDao.deleteTripSession(tripId)
    }

    override suspend fun markPlaceUsed(placeId: String, usedAt: kotlinx.datetime.Instant) {
        savedPlaceDao.getSavedPlaceById(placeId)?.let { place ->
            savedPlaceDao.upsertSavedPlace(place.copy(lastUsedAt = usedAt))
        }
    }

    override suspend fun saveTrip(command: SaveTripCommand) {
        tripDao.upsertTrip(
            TripEntity(
                id = command.id,
                destinationId = command.destinationId,
                alertLeadMinutes = command.alertLeadMinutes,
                alertTriggerMode = command.alertTriggerMode,
                alertDistanceMeters = command.alertDistanceMeters,
                alertIntensity = command.alertIntensity,
                alertMode = command.alertMode,
                createdAt = command.createdAt,
                completedAt = command.completedAt,
            ),
        )
    }

    override suspend fun saveTripSession(command: SaveTripSessionCommand) {
        tripSessionDao.upsertTripSession(
            TripSessionEntity(
                tripId = command.tripId,
                state = command.state,
                monitoringMode = command.monitoringMode,
                alertStage = command.alertStage,
                lastKnownLat = command.lastKnownLat,
                lastKnownLng = command.lastKnownLng,
                lastEtaMinutes = command.lastEtaMinutes,
                confidence = command.confidence,
                geofenceIds = command.geofenceIds,
                updatedAt = command.updatedAt,
            ),
        )
    }
}

private fun com.nearwake.core.database.entity.SavedPlaceEntity.toPersistedSavedPlace(): PersistedSavedPlace =
    PersistedSavedPlace(
        id = id,
        name = name,
        address = address,
        lat = lat,
        lng = lng,
    )

private fun TripEntity.toPersistedTrip(): PersistedTrip =
    PersistedTrip(
        id = id,
        destinationId = destinationId,
        alertLeadMinutes = alertLeadMinutes,
        alertTriggerMode = alertTriggerMode,
        alertDistanceMeters = alertDistanceMeters,
        alertIntensity = alertIntensity,
        alertMode = alertMode,
        createdAt = createdAt,
        completedAt = completedAt,
    )

private fun TripSessionEntity.toPersistedTripSession(): PersistedTripSession =
    PersistedTripSession(
        tripId = tripId,
        state = state,
        monitoringMode = monitoringMode,
        alertStage = alertStage,
        lastEtaMinutes = lastEtaMinutes,
        confidence = confidence,
    )
