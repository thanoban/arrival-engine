package com.nearwake.data.alerts.worker

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.google.common.truth.Truth.assertThat
import com.nearwake.core.database.dao.TripSessionDao
import com.nearwake.core.database.entity.TripSessionEntity
import com.nearwake.core.testing.TripEngineTestFixtures
import com.nearwake.data.alerts.TripMonitoringStarter
import com.nearwake.data.analytics.DiagnosticsLogger
import com.nearwake.domain.trip.model.Confidence
import com.nearwake.domain.trip.model.MonitoringMode
import com.nearwake.domain.trip.model.TripState
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class TripRecoveryWorkerTest {
    private val appContext = mockk<Context>(relaxed = true)
    private val workerParams = mockk<WorkerParameters>(relaxed = true)
    private val tripSessionDao = mockk<TripSessionDao>()
    private val tripMonitoringStarter = mockk<TripMonitoringStarter>(relaxed = true)
    private val diagnosticsLogger = mockk<DiagnosticsLogger>()

    @Test
    fun `doWork returns success without side effects when there is no active session`() = runTest {
        coEvery { tripSessionDao.getActiveTripSession() } returns null
        coJustRun { diagnosticsLogger.log(any(), any(), any()) }

        val result = worker().doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.success())
        coVerify(exactly = 1) { tripSessionDao.getActiveTripSession() }
        coVerify(exactly = 0) { diagnosticsLogger.log(any(), any(), any()) }
        verify(exactly = 0) { tripMonitoringStarter.start(any(), any()) }
    }

    @Test
    fun `doWork restarts monitoring and logs recovery for an active session`() = runTest {
        val session = sessionEntity(state = TripState.MonitoringApproach)
        coEvery { tripSessionDao.getActiveTripSession() } returns session
        coJustRun { diagnosticsLogger.log(any(), any(), any()) }

        val result = worker().doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.success())
        verify(exactly = 1) { tripMonitoringStarter.start(appContext, session.tripId) }
        coVerify(exactly = 1) {
            diagnosticsLogger.log(
                eventType = "process_death_recovered",
                tripId = session.tripId,
                payload = any(),
            )
        }
    }

    @Test
    fun `doWork ignores terminal sessions returned by persistence`() = runTest {
        coEvery { tripSessionDao.getActiveTripSession() } returns sessionEntity(state = TripState.Completed)
        coJustRun { diagnosticsLogger.log(any(), any(), any()) }

        val result = worker().doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.success())
        coVerify(exactly = 0) { diagnosticsLogger.log(any(), any(), any()) }
        verify(exactly = 0) { tripMonitoringStarter.start(any(), any()) }
    }

    private fun worker(): TripRecoveryWorker = TripRecoveryWorker(
        appContext = appContext,
        params = workerParams,
        tripSessionDao = tripSessionDao,
        tripMonitoringStarter = tripMonitoringStarter,
        diagnosticsLogger = diagnosticsLogger,
    )

    private fun sessionEntity(
        tripId: String = "trip-123",
        state: TripState,
    ): TripSessionEntity = TripSessionEntity(
        tripId = tripId,
        state = state,
        monitoringMode = MonitoringMode.GEOFENCE_ONLY,
        lastKnownLat = null,
        lastKnownLng = null,
        lastEtaMinutes = 12,
        confidence = Confidence.HIGH,
        geofenceIds = TripEngineTestFixtures.tripSession(tripId = tripId).geofenceIds,
        updatedAt = TripEngineTestFixtures.fixedInstant,
    )
}
