package com.nearwake.data.alerts.di

import com.nearwake.core.remoteconfig.RemoteConfigRepository
import com.nearwake.core.remoteconfig.StaticRemoteConfigRepository
import com.nearwake.core.remoteconfig.ThresholdConfig
import com.nearwake.domain.trip.engine.TripEngine
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AlertsModule {
    @Provides
    @Singleton
    fun provideTripEngine(): TripEngine = TripEngine()

    @Provides
    @Singleton
    fun provideRemoteConfigRepository(): RemoteConfigRepository = StaticRemoteConfigRepository()

    @Provides
    @Singleton
    fun provideThresholdConfig(
        remoteConfigRepository: RemoteConfigRepository,
    ): ThresholdConfig = remoteConfigRepository.currentThresholdConfig()
}
