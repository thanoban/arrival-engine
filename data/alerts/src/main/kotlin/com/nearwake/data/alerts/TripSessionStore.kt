package com.nearwake.data.alerts

import com.nearwake.core.database.dao.TripSessionDao
import com.nearwake.core.database.entity.TripSessionEntity
import com.nearwake.domain.trip.model.AlertStage
import com.nearwake.domain.trip.model.TripSession
import com.nearwake.domain.trip.model.TripState
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.datetime.Clock

@Singleton
class TripSessionStore @Inject constructor(
    private val tripSessionDao: TripSessionDao,
) {
    suspend fun loadOrCreate(tripId: String): TripSession {
        return tripSessionDao.getTripSessionById(tripId)?.toModel()
            ?: TripSession(
                tripId = tripId,
                state = TripState.Armed,
                monitoringMode = TripState.Armed.defaultMonitoringMode(),
                alertStage = AlertStage.MONITORING,
                updatedAt = Clock.System.now(),
            ).also { session ->
                save(session)
            }
    }

    suspend fun save(session: TripSession) {
        tripSessionDao.upsertTripSession(session.toEntity())
    }

    private fun TripSessionEntity.toModel(): TripSession =
        TripSession(
            tripId = tripId,
            state = state,
            monitoringMode = monitoringMode,
            alertStage = alertStage,
            lastKnownLat = lastKnownLat,
            lastKnownLng = lastKnownLng,
            lastEtaMinutes = lastEtaMinutes,
            confidence = confidence,
            geofenceIds = geofenceIds,
            updatedAt = updatedAt,
        )

    private fun TripSession.toEntity(): TripSessionEntity =
        TripSessionEntity(
            tripId = tripId,
            state = state,
            monitoringMode = monitoringMode,
            alertStage = alertStage,
            lastKnownLat = lastKnownLat,
            lastKnownLng = lastKnownLng,
            lastEtaMinutes = lastEtaMinutes,
            confidence = confidence,
            geofenceIds = geofenceIds,
            updatedAt = updatedAt,
        )

    private fun TripState.defaultMonitoringMode() = when (this) {
        TripState.MonitoringLowPower,
        TripState.Recovery -> com.nearwake.domain.trip.model.MonitoringMode.BALANCED

        TripState.MonitoringApproach -> com.nearwake.domain.trip.model.MonitoringMode.PRECISE_BURST

        TripState.Idle,
        TripState.Armed,
        TripState.WaitingForMovement,
        TripState.Alerting,
        TripState.Completed,
        TripState.Cancelled,
        TripState.FailedGracefully -> com.nearwake.domain.trip.model.MonitoringMode.GEOFENCE_ONLY
    }
}
