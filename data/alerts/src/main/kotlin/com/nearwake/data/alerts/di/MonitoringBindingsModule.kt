package com.nearwake.data.alerts.di

import com.nearwake.data.alerts.TripMonitoringGatewayImpl
import com.nearwake.ports.monitoring.TripMonitoringGateway
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class MonitoringBindingsModule {
    @Binds
    @Singleton
    abstract fun bindTripMonitoringGateway(
        impl: TripMonitoringGatewayImpl,
    ): TripMonitoringGateway
}
