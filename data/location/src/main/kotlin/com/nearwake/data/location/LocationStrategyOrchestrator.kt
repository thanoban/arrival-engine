package com.nearwake.data.location

import com.nearwake.domain.location.model.LatLng
import com.nearwake.domain.trip.model.MonitoringMode
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow

@Singleton
class LocationStrategyOrchestrator @Inject constructor(
    private val fusedLocationDataSource: FusedLocationDataSource,
) {
    private val mutableMode = MutableStateFlow(MonitoringMode.GEOFENCE_ONLY)

    val monitoringMode: StateFlow<MonitoringMode> = mutableMode

    fun escalate(to: MonitoringMode): Flow<LatLng> {
        mutableMode.value = to
        return when (to) {
            MonitoringMode.GEOFENCE_ONLY -> emptyFlow()
            MonitoringMode.BALANCED -> fusedLocationDataSource.startBalancedUpdates()
            MonitoringMode.PRECISE_BURST -> fusedLocationDataSource.startPreciseBurst()
        }
    }

    suspend fun deescalate() {
        fusedLocationDataSource.stopUpdates()
        mutableMode.value = MonitoringMode.GEOFENCE_ONLY
    }

    suspend fun stop() {
        fusedLocationDataSource.stopUpdates()
        mutableMode.value = MonitoringMode.GEOFENCE_ONLY
    }
}
