package com.nearwake.data.alerts

import android.app.Notification
import com.google.common.truth.Truth.assertThat
import com.nearwake.data.analytics.DiagnosticsLogger
import com.nearwake.domain.location.model.LatLng
import com.nearwake.domain.routing.model.RouteSnapshot
import com.nearwake.domain.routing.model.Stop
import com.nearwake.domain.routing.model.TransferPoint
import com.nearwake.domain.trip.model.AlertIntensity
import com.nearwake.domain.trip.model.AlertMode
import com.nearwake.domain.trip.model.AlertStage
import com.nearwake.domain.trip.model.MonitoringMode
import com.nearwake.domain.trip.model.TripRule
import com.nearwake.domain.trip.model.TripSession
import com.nearwake.domain.trip.model.TripState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import org.junit.jupiter.api.Test

class TripMonitoringFeedbackCoordinatorTest {
    private val notificationHelper = mockk<NotificationHelper>()
    private val diagnosticsLogger = mockk<DiagnosticsLogger>()
    private val tripMonitoringRuntime = mockk<TripMonitoringRuntime>()

    private val coordinator = TripMonitoringFeedbackCoordinator(
        notificationHelper = notificationHelper,
        diagnosticsLogger = diagnosticsLogger,
        tripMonitoringRuntime = tripMonitoringRuntime,
    )

    @Test
    fun `onSessionUpdated fires stage notification when stage advances`() = runTest {
        val notification = mockk<Notification>()
        every {
            notificationHelper.buildStageNotification(
                tripId = "trip-1",
                destinationName = "Colombo Fort",
                stage = AlertStage.IMMINENT,
                mode = AlertMode.ACTIVE,
            )
        } returns notification
        every { notificationHelper.notify(46, notification) } just runs
        coEvery { diagnosticsLogger.log(any(), any(), any()) } just runs

        coordinator.onSessionUpdated(
            previousSession = session(alertStage = AlertStage.APPROACH),
            currentSession = session(alertStage = AlertStage.IMMINENT),
            context = monitoredContext(),
        )

        verify(exactly = 1) {
            notificationHelper.buildStageNotification(
                tripId = "trip-1",
                destinationName = "Colombo Fort",
                stage = AlertStage.IMMINENT,
                mode = AlertMode.ACTIVE,
            )
        }
        verify(exactly = 1) { notificationHelper.notify(46, notification) }
    }

    @Test
    fun `onSessionUpdated suppresses duplicate transfer cues for same checkpoint`() = runTest {
        val notification = mockk<Notification>()
        every {
            notificationHelper.buildTransferNotification(
                tripId = "trip-1",
                stopName = "Pettah",
                lineName = "138",
                remainingMinutes = 2,
            )
        } returns notification
        every { notificationHelper.notify(44, notification) } just runs
        coEvery { diagnosticsLogger.log(any(), any(), any()) } just runs

        val currentSession = session(lastEtaMinutes = 20)
        val context = monitoredContext(
            routeSnapshot = routeSnapshot(),
        )

        coordinator.onSessionUpdated(
            previousSession = null,
            currentSession = currentSession,
            context = context,
        )
        coordinator.onSessionUpdated(
            previousSession = currentSession,
            currentSession = currentSession,
            context = context,
        )

        verify(exactly = 1) {
            notificationHelper.buildTransferNotification(
                tripId = "trip-1",
                stopName = "Pettah",
                lineName = "138",
                remainingMinutes = 2,
            )
        }
        verify(exactly = 1) { notificationHelper.notify(44, notification) }
        coVerify(exactly = 1) {
            diagnosticsLogger.log(
                eventType = "transfer_alert_fired",
                tripId = "trip-1",
                payload = any(),
            )
        }
    }

    @Test
    fun `resolveAlertDistanceMeters returns null and logs when session has no location`() = runTest {
        coEvery { diagnosticsLogger.log(any(), any(), any()) } just runs

        val distanceMeters = coordinator.resolveAlertDistanceMeters(
            session = session(lastKnownLat = null, lastKnownLng = null),
            context = monitoredContext(),
        )

        assertThat(distanceMeters).isNull()
        coVerify(exactly = 1) {
            diagnosticsLogger.log(
                eventType = "alert_distance_unavailable",
                tripId = "trip-1",
                payload = any(),
            )
        }
        verify(exactly = 0) {
            tripMonitoringRuntime.distanceToDestination(any(), any(), any())
        }
    }

    private fun session(
        alertStage: AlertStage = AlertStage.MONITORING,
        lastKnownLat: Double? = 6.9271,
        lastKnownLng: Double? = 79.8612,
        lastEtaMinutes: Int? = null,
    ): TripSession =
        TripSession(
            tripId = "trip-1",
            state = TripState.MonitoringApproach,
            monitoringMode = MonitoringMode.BALANCED,
            alertStage = alertStage,
            lastKnownLat = lastKnownLat,
            lastKnownLng = lastKnownLng,
            lastEtaMinutes = lastEtaMinutes,
            updatedAt = Clock.System.now(),
        )

    private fun monitoredContext(
        routeSnapshot: RouteSnapshot? = null,
    ): MonitoredTripContext =
        MonitoredTripContext(
            tripId = "trip-1",
            destinationName = "Colombo Fort",
            alertIntensity = AlertIntensity.STANDARD,
            alertMode = AlertMode.ACTIVE,
            destination = LatLng(lat = 6.9344, lng = 79.8428),
            tripRule = TripRule(alertLeadMinutes = 5),
            geofenceIds = listOf("trip-1:approach", "trip-1:destination"),
            hasCachedRoute = routeSnapshot != null,
            routeSnapshot = routeSnapshot,
            initialEtaMinutes = 20,
        )

    private fun routeSnapshot(): RouteSnapshot =
        RouteSnapshot(
            tripId = "trip-1",
            stops = listOf(
                Stop(name = "Maharagama", lat = 6.8480, lng = 79.9265, order = 0),
                Stop(name = "Pettah", lat = 6.9396, lng = 79.8500, order = 1),
                Stop(name = "Colombo Fort", lat = 6.9344, lng = 79.8428, order = 2),
            ),
            transfers = listOf(
                TransferPoint(
                    stop = Stop(name = "Pettah", lat = 6.9396, lng = 79.8500, order = 1),
                    lineName = "138",
                    arrivalMinutes = 12,
                ),
            ),
            totalDurationMinutes = 30,
            fetchedAt = Clock.System.now(),
            isStale = false,
        )
}
