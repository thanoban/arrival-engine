package com.nearwake.data.location.di

import com.nearwake.core.common.region.RegionSignalDataSource
import com.nearwake.core.common.region.ResolvedRegionRepository
import com.nearwake.data.location.AndroidRegionSignalDataSource
import com.nearwake.data.location.GeofenceRepositoryImpl
import com.nearwake.data.location.GooglePlacesSearchRepository
import com.nearwake.data.location.LocationRepositoryImpl
import com.nearwake.data.location.ResolvedRegionRepositoryImpl
import com.nearwake.data.location.SamplePlaceSearchRepository
import com.nearwake.domain.location.repository.GeofenceRepository
import com.nearwake.domain.location.repository.LocationRepository
import com.nearwake.domain.location.repository.PlaceSearchRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PlaceSearchModule {
    @Provides
    @Singleton
    fun providePlaceSearchRepository(
        googlePlacesSearchRepository: GooglePlacesSearchRepository,
        samplePlaceSearchRepository: SamplePlaceSearchRepository,
    ): PlaceSearchRepository = if (com.nearwake.data.location.BuildConfig.MAPS_API_KEY.isBlank()) {
        samplePlaceSearchRepository
    } else {
        googlePlacesSearchRepository
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class LocationModule {
    @Binds
    abstract fun bindRegionSignalDataSource(
        impl: AndroidRegionSignalDataSource,
    ): RegionSignalDataSource

    @Binds
    abstract fun bindResolvedRegionRepository(
        impl: ResolvedRegionRepositoryImpl,
    ): ResolvedRegionRepository

    @Binds
    abstract fun bindLocationRepository(
        impl: LocationRepositoryImpl,
    ): LocationRepository

    @Binds
    abstract fun bindGeofenceRepository(
        impl: GeofenceRepositoryImpl,
    ): GeofenceRepository
}
