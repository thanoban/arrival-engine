package com.nearwake.data.alerts

import android.content.Context
import android.content.Intent
import com.nearwake.ports.monitoring.TripMonitoringGateway
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TripMonitoringGatewayImpl @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val alertOrchestrator: AlertOrchestrator,
) : TripMonitoringGateway {
    override fun startMonitoring(tripId: String) {
        TripMonitoringService.start(appContext, tripId)
    }

    override fun stopMonitoring() {
        alertOrchestrator.stopActiveAlert()
        appContext.stopService(Intent(appContext, TripMonitoringService::class.java))
    }
}
