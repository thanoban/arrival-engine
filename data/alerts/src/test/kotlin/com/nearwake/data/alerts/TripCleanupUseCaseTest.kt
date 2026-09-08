package com.nearwake.data.alerts

import com.google.common.truth.Truth.assertThat
import com.nearwake.data.location.GeofenceDataSource
import com.nearwake.data.location.LocationStrategyOrchestrator
import com.nearwake.data.motion.ActivityRecognitionDataSource
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.awaitCancellation
import org.junit.jupiter.api.Test

class TripCleanupUseCaseTest {
    private val geofenceDataSource = mockk<GeofenceDataSource>()
    private val locationStrategyOrchestrator = mockk<LocationStrategyOrchestrator>()
    private val activityRecognitionDataSource = mockk<ActivityRecognitionDataSource>()

    private val useCase = TripCleanupUseCase(
        geofenceDataSource = geofenceDataSource,
        locationStrategyOrchestrator = locationStrategyOrchestrator,
        activityRecognitionDataSource = activityRecognitionDataSource,
    )

    @Test
    fun `invoke removes geofences stops location and unregisters activity transitions`() = runTest {
        coEvery { geofenceDataSource.removeAllGeofences() } returns Result.success(Unit)
        coJustRun { locationStrategyOrchestrator.stop() }
        coJustRun { activityRecognitionDataSource.unregisterActivityTransitions() }

        useCase()

        coVerify(exactly = 1) { geofenceDataSource.removeAllGeofences() }
        coVerify(exactly = 1) { locationStrategyOrchestrator.stop() }
        coVerify(exactly = 1) { activityRecognitionDataSource.unregisterActivityTransitions() }
    }

    @Test
    fun `invoke propagates geofence removal failures`() = runTest {
        val failure = IllegalStateException("geofence removal failed")
        coEvery { geofenceDataSource.removeAllGeofences() } returns Result.failure(failure)
        coJustRun { locationStrategyOrchestrator.stop() }
        coJustRun { activityRecognitionDataSource.unregisterActivityTransitions() }

        val thrown = runCatching { useCase() }.exceptionOrNull()

        assertThat(thrown).isInstanceOf(IllegalStateException::class.java)
        assertThat(thrown).hasMessageThat().isEqualTo(failure.message)
        coVerify(exactly = 1) { geofenceDataSource.removeAllGeofences() }
        coVerify(exactly = 1) { locationStrategyOrchestrator.stop() }
        coVerify(exactly = 1) { activityRecognitionDataSource.unregisterActivityTransitions() }
    }

    @Test
    fun `all resources are attempted when multiple releases fail`() = runTest {
        val locationFailure = IllegalStateException("GPS removal failed")
        val geofenceFailure = IllegalStateException("geofence removal failed")
        coEvery { locationStrategyOrchestrator.stop() } throws locationFailure
        coEvery { geofenceDataSource.removeAllGeofences() } returns Result.failure(geofenceFailure)
        coJustRun { activityRecognitionDataSource.unregisterActivityTransitions() }

        val thrown = runCatching { useCase() }.exceptionOrNull()

        assertThat(thrown).hasMessageThat().isEqualTo(locationFailure.message)
        val suppressedMessages = generateSequence(thrown) { it.cause }
            .flatMap { it.suppressed.asSequence() }.map { it.message }.toList()
        assertThat(suppressedMessages).contains(geofenceFailure.message)
        coVerify(exactly = 1) { activityRecognitionDataSource.unregisterActivityTransitions() }
    }

    @Test
    fun `a hung release cannot prevent the remaining cleanup`() = runTest {
        coEvery { locationStrategyOrchestrator.stop() } coAnswers { awaitCancellation() }
        coEvery { geofenceDataSource.removeAllGeofences() } returns Result.success(Unit)
        coJustRun { activityRecognitionDataSource.unregisterActivityTransitions() }

        val thrown = runCatching { useCase() }.exceptionOrNull()

        assertThat(thrown).isInstanceOf(kotlinx.coroutines.TimeoutCancellationException::class.java)
        coVerify(exactly = 1) { geofenceDataSource.removeAllGeofences() }
        coVerify(exactly = 1) { activityRecognitionDataSource.unregisterActivityTransitions() }
    }
}
