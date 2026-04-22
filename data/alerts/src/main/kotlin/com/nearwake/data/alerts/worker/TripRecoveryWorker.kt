package com.nearwake.data.alerts.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.nearwake.core.database.dao.TripSessionDao
import com.nearwake.data.alerts.TripMonitoringService
import com.nearwake.data.analytics.DiagnosticsLogger
import com.nearwake.domain.trip.model.isActive
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@HiltWorker
class TripRecoveryWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val tripSessionDao: TripSessionDao,
    private val diagnosticsLogger: DiagnosticsLogger,
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val session = tripSessionDao.getActiveTripSession() ?: return Result.success()
        if (!session.state.isActiveState()) return Result.success()

        TripMonitoringService.start(applicationContext, session.tripId)
        diagnosticsLogger.log(
            eventType = "process_death_recovered",
            tripId = session.tripId,
            payload = buildJsonObject {
                put("state_restored", session.state.name)
            },
        )
        return Result.success()
    }

    private fun com.nearwake.domain.trip.model.TripState.isActiveState(): Boolean =
        this == com.nearwake.domain.trip.model.TripState.Armed ||
            this == com.nearwake.domain.trip.model.TripState.WaitingForMovement ||
            this == com.nearwake.domain.trip.model.TripState.MonitoringLowPower ||
            this == com.nearwake.domain.trip.model.TripState.MonitoringApproach ||
            this == com.nearwake.domain.trip.model.TripState.Alerting ||
            this == com.nearwake.domain.trip.model.TripState.Recovery
}
