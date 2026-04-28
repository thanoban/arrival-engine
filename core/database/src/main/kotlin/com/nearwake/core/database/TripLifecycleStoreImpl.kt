package com.nearwake.core.database

import com.nearwake.core.database.dao.SavedPlaceDao
import com.nearwake.core.database.dao.TripDao
import com.nearwake.core.database.dao.TripSessionDao
import com.nearwake.core.database.entity.TripEntity
import com.nearwake.core.database.entity.TripSessionEntity
import com.nearwake.ports.persistence.PersistedSavedPlace
import com.nearwake.ports.persistence.PersistedTrip
import com.nearwake.ports.persistence.SaveTripCommand
import com.nearwake.ports.persistence.SaveTripSessionCommand
import com.nearwake.ports.persistence.TripLifecycleStore
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TripLifecycleStoreImpl @Inject constructor(
    private val savedPlaceDao: SavedPlaceDao,
    private val tripDao: TripDao,
    private val tripSessionDao: TripSessionDao,
) : TripLifecycleStore {
    override suspend fun getSavedPlace(placeId: String): PersistedSavedPlace? =
        savedPlaceDao.getSavedPlaceById(placeId)?.let { place ->
            PersistedSavedPlace(
                id = place.id,
                name = place.name,
                address = place.address,
                lat = place.lat,
                lng = place.lng,
            )
        }

    override suspend fun getTrip(tripId: String): PersistedTrip? =
        tripDao.getTripById(tripId)?.let { trip ->
            PersistedTrip(
                id = trip.id,
                destinationId = trip.destinationId,
                alertLeadMinutes = trip.alertLeadMinutes,
                alertIntensity = trip.alertIntensity,
                alertMode = trip.alertMode,
            )
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
