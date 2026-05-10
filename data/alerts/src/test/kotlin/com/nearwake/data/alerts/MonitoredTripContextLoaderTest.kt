package com.nearwake.data.alerts

import com.google.common.truth.Truth.assertThat
import com.nearwake.core.database.dao.SavedPlaceDao
import com.nearwake.core.database.dao.TripDao
import com.nearwake.core.database.entity.SavedPlaceEntity
import com.nearwake.core.database.entity.TripEntity
import com.nearwake.data.analytics.DiagnosticsLogger
import com.nearwake.domain.routing.model.RouteSnapshot
import com.nearwake.domain.routing.repository.RoutingRepository
import com.nearwake.domain.trip.model.AlertIntensity
import com.nearwake.domain.trip.model.AlertMode
import com.nearwake.domain.trip.model.AlertTriggerMode
import com.nearwake.ports.analytics.NearWakeAnalytics
import io.mockk.coJustRun
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import org.junit.jupiter.api.Test

class MonitoredTripContextLoaderTest {
    private val tripDao = mockk<TripDao>()
    private val savedPlaceDao = mockk<SavedPlaceDao>()
    private val routingRepository = mockk<RoutingRepository>()
    private val tripMonitoringRuntime = mockk<TripMonitoringRuntime>()
    private val diagnosticsLogger = mockk<DiagnosticsLogger>()
    private val analytics = mockk<NearWakeAnalytics>(relaxed = true)

    private val loader = MonitoredTripContextLoader(
        tripDao = tripDao,
        savedPlaceDao = savedPlaceDao,
        routingRepository = routingRepository,
        tripMonitoringRuntime = tripMonitoringRuntime,
        diagnosticsLogger = diagnosticsLogger,
        analytics = analytics,
    )

    @Test
    fun `load builds monitored context from trip destination and cached route`() = runTest {
        val trip = tripEntity()
        val destination = savedPlaceEntity()
        val cachedRoute = mockk<RouteSnapshot> {
            every { totalDurationMinutes } returns 32
        }
        val expectedContext = mockk<MonitoredTripContext>()
        coEvery { tripDao.getTripById("trip-1") } returns trip
        coEvery { savedPlaceDao.getSavedPlaceById("place-1") } returns destination
        coEvery { routingRepository.getCachedRoute("trip-1") } returns cachedRoute
        every {
            tripMonitoringRuntime.buildContext(
                tripId = "trip-1",
                destinationName = "Colombo Fort",
                alertLeadMinutes = 5,
                alertTriggerMode = AlertTriggerMode.BOTH,
                alertDistanceMeters = 650,
                alertIntensity = AlertIntensity.STANDARD,
                alertMode = AlertMode.SLEEP,
                destination = any(),
                hasCachedRoute = true,
                routeSnapshot = cachedRoute,
                initialEtaMinutes = 32,
                batterySaverMode = true,
            )
        } returns expectedContext
        coJustRun { diagnosticsLogger.log(any(), any(), any()) }

        val context = loader.load(
            tripId = "trip-1",
            batterySaverMode = true,
        )

        assertThat(context).isSameInstanceAs(expectedContext)
    }

    @Test
    fun `load returns null when trip does not exist`() = runTest {
        coEvery { tripDao.getTripById("trip-1") } returns null
        coJustRun { diagnosticsLogger.log(any(), any(), any()) }

        val context = loader.load(
            tripId = "trip-1",
            batterySaverMode = false,
        )

        assertThat(context).isNull()
        coVerify(exactly = 0) { diagnosticsLogger.log(any(), any(), any()) }
    }

    @Test
    fun `load logs and returns null when dao throws`() = runTest {
        coEvery { tripDao.getTripById("trip-1") } throws IllegalStateException("db offline")
        coJustRun { diagnosticsLogger.log(any(), any(), any()) }

        val context = loader.load(
            tripId = "trip-1",
            batterySaverMode = false,
        )

        assertThat(context).isNull()
        coVerify(exactly = 1) {
            diagnosticsLogger.log(
                eventType = "monitoring_service_trip_context_failed",
                tripId = "trip-1",
                payload = any(),
            )
        }
    }

    private fun tripEntity(): TripEntity =
        TripEntity(
            id = "trip-1",
            destinationId = "place-1",
            alertLeadMinutes = 5,
            alertTriggerMode = AlertTriggerMode.BOTH,
            alertDistanceMeters = 650,
            alertIntensity = AlertIntensity.STANDARD,
            alertMode = AlertMode.SLEEP,
            createdAt = Clock.System.now(),
        )

    private fun savedPlaceEntity(): SavedPlaceEntity =
        SavedPlaceEntity(
            id = "place-1",
            name = "Colombo Fort",
            address = "Fort, Colombo",
            lat = 6.9344,
            lng = 79.8428,
        )
}
