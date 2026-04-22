package com.nearwake.data.location

import com.google.common.truth.Truth.assertThat
import com.nearwake.core.testing.TripEngineTestFixtures
import com.nearwake.domain.trip.model.MonitoringMode
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class LocationStrategyOrchestratorTest {
    private val balancedLocation = TripEngineTestFixtures.latLng()
    private val preciseLocation = TripEngineTestFixtures.latLng(lat = 7.1808, lng = 79.8841)

    @Test
    fun `escalate to balanced switches mode and returns balanced updates`() = runTest {
        val fusedLocationDataSource = fakeFusedLocationDataSource()
        val orchestrator = LocationStrategyOrchestrator(fusedLocationDataSource)
        val result = orchestrator.escalate(MonitoringMode.BALANCED).first()

        assertThat(result).isEqualTo(balancedLocation)
        assertThat(orchestrator.monitoringMode.value).isEqualTo(MonitoringMode.BALANCED)
    }

    @Test
    fun `escalate to precise burst switches mode and returns precise updates`() = runTest {
        val fusedLocationDataSource = fakeFusedLocationDataSource()
        val orchestrator = LocationStrategyOrchestrator(fusedLocationDataSource)
        val result = orchestrator.escalate(MonitoringMode.PRECISE_BURST).first()

        assertThat(result).isEqualTo(preciseLocation)
        assertThat(orchestrator.monitoringMode.value).isEqualTo(MonitoringMode.PRECISE_BURST)
    }

    @Test
    fun `deescalate stops updates and resets mode`() = runTest {
        val fusedLocationDataSource = fakeFusedLocationDataSource()
        val orchestrator = LocationStrategyOrchestrator(fusedLocationDataSource)
        orchestrator.escalate(MonitoringMode.BALANCED)

        orchestrator.deescalate()

        coVerify(exactly = 1) { fusedLocationDataSource.stopUpdates() }
        assertThat(orchestrator.monitoringMode.value).isEqualTo(MonitoringMode.GEOFENCE_ONLY)
    }

    @Test
    fun `stop stops updates and resets mode`() = runTest {
        val fusedLocationDataSource = fakeFusedLocationDataSource()
        val orchestrator = LocationStrategyOrchestrator(fusedLocationDataSource)
        orchestrator.escalate(MonitoringMode.PRECISE_BURST)

        orchestrator.stop()

        coVerify(exactly = 1) { fusedLocationDataSource.stopUpdates() }
        assertThat(orchestrator.monitoringMode.value).isEqualTo(MonitoringMode.GEOFENCE_ONLY)
    }

    private fun fakeFusedLocationDataSource(): FusedLocationDataSource =
        mockk {
            every { startBalancedUpdates(any()) } returns flowOf(balancedLocation)
            every { startPreciseBurst(any()) } returns flowOf(preciseLocation)
            coJustRun { stopUpdates() }
        }
}
