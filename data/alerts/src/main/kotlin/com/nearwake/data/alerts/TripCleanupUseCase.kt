package com.nearwake.data.alerts

import com.nearwake.data.location.GeofenceDataSource
import com.nearwake.data.location.LocationStrategyOrchestrator
import com.nearwake.data.motion.ActivityRecognitionDataSource
import javax.inject.Inject
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

class TripCleanupUseCase @Inject constructor(
    private val geofenceDataSource: GeofenceDataSource,
    private val locationStrategyOrchestrator: LocationStrategyOrchestrator,
    private val activityRecognitionDataSource: ActivityRecognitionDataSource,
) {
    suspend operator fun invoke() {
        val failure = withContext(NonCancellable) { releaseResources() }
        failure?.let { throw it }
    }

    private suspend fun releaseResources(): Exception? {
        var failure: Exception? = null
        // Each resource must get a release attempt even if another API fails or hangs.
        val releases: List<suspend () -> Unit> = listOf(
            { locationStrategyOrchestrator.stop() },
            { geofenceDataSource.removeAllGeofences().getOrThrow() },
            { activityRecognitionDataSource.unregisterActivityTransitions() },
        )
        for (release in releases) {
            try {
                withTimeout(10_000L) { release() }
            } catch (error: Exception) {
                if (failure == null) failure = error
                else if (failure !== error) failure.addSuppressed(error)
            }
        }
        return failure
    }
}
