package com.nearwake.data.alerts

import com.nearwake.data.location.GeofenceDataSource
import com.nearwake.data.location.LocationStrategyOrchestrator
import com.nearwake.data.motion.ActivityRecognitionDataSource
import javax.inject.Inject

class TripCleanupUseCase @Inject constructor(
    private val geofenceDataSource: GeofenceDataSource,
    private val locationStrategyOrchestrator: LocationStrategyOrchestrator,
    private val activityRecognitionDataSource: ActivityRecognitionDataSource,
) {
    suspend operator fun invoke() {
        geofenceDataSource.removeAllGeofences().getOrThrow()
        locationStrategyOrchestrator.stop()
        activityRecognitionDataSource.unregisterActivityTransitions()
    }
}
