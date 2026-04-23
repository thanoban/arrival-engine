package com.nearwake.data.alerts

import android.content.Context
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TripMonitoringStarter @Inject constructor() {
    fun start(context: Context, tripId: String) {
        TripMonitoringService.start(context, tripId)
    }
}
