package com.nearwake.ports.monitoring

interface TripMonitoringGateway {
    fun startMonitoring(tripId: String)

    fun stopMonitoring()

    suspend fun acknowledgeAlert(tripId: String)
}
