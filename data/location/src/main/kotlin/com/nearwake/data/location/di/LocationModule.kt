package com.nearwake.data.location.di

import com.nearwake.data.location.GeofenceRepositoryImpl
import com.nearwake.data.location.LocationRepositoryImpl
import com.nearwake.domain.location.repository.GeofenceRepository
import com.nearwake.domain.location.repository.LocationRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class LocationModule {
    @Binds
    abstract fun bindLocationRepository(
        impl: LocationRepositoryImpl,
    ): LocationRepository

    @Binds
    abstract fun bindGeofenceRepository(
        impl: GeofenceRepositoryImpl,
    ): GeofenceRepository
}
