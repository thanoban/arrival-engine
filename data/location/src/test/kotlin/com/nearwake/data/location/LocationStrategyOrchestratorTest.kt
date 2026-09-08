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
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.flow
import io.mockk.verify
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
            every { startBalancedUpdates(any(), any()) } returns flowOf(balancedLocation)
            every { startPreciseBurst(any()) } returns flowOf(preciseLocation)
            coJustRun { stopUpdates() }
        }

    @Test
    fun `completed precise burst continues balanced monitoring`() = runTest {
        val source = fakeFusedLocationDataSource()
        val orchestrator = LocationStrategyOrchestrator(source)

        val locations = orchestrator.escalate(MonitoringMode.PRECISE_BURST, 200f).toList()

        assertThat(locations).containsExactly(preciseLocation, balancedLocation).inOrder()
        assertThat(orchestrator.monitoringMode.value).isEqualTo(MonitoringMode.BALANCED)
        verify { source.startBalancedUpdates(any(), 200f) }
    }

    @Test
    fun `cancelled precise collection does not start a new balanced request`() = runTest {
        val source = fakeFusedLocationDataSource()
        val orchestrator = LocationStrategyOrchestrator(source)

        orchestrator.escalate(MonitoringMode.PRECISE_BURST).first()

        verify(exactly = 0) { source.startBalancedUpdates(any(), any()) }
    }

    @Test
    fun `precise failure is surfaced to the service instead of hidden`() = runTest {
        val source = fakeFusedLocationDataSource()
        val failure = SecurityException("Permission revoked")
        every { source.startPreciseBurst(any()) } returns flow { throw failure }

        val thrown = runCatching {
            LocationStrategyOrchestrator(source).escalate(MonitoringMode.PRECISE_BURST).toList()
        }.exceptionOrNull()

        assertThat(thrown).isSameInstanceAs(failure)
        verify(exactly = 0) { source.startBalancedUpdates(any(), any()) }
    }
}
