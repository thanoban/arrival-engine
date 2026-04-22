package com.nearwake.data.alerts.di

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
}
