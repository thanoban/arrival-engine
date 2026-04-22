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

        val thrown = runCatching { useCase() }.exceptionOrNull()

        assertThat(thrown).isSameInstanceAs(failure)
        coVerify(exactly = 1) { geofenceDataSource.removeAllGeofences() }
        coVerify(exactly = 0) { locationStrategyOrchestrator.stop() }
        coVerify(exactly = 0) { activityRecognitionDataSource.unregisterActivityTransitions() }
    }
}
