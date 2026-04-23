package com.nearwake.data.routing.di

import com.nearwake.data.routing.BuildConfig
import com.nearwake.data.routing.GoogleTransitDataSource
import com.nearwake.data.routing.NoOpRoutingDataSource
import com.nearwake.data.routing.RoutingRepositoryImpl
import com.nearwake.domain.routing.RoutingDataSource
import com.nearwake.domain.routing.repository.RoutingRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient

@Module
@InstallIn(SingletonComponent::class)
object RoutingModule {
    @Provides
    @Singleton
    fun provideRoutingDataSource(
        googleTransitDataSource: GoogleTransitDataSource,
        noOpRoutingDataSource: NoOpRoutingDataSource,
    ): RoutingDataSource = if (BuildConfig.MAPS_API_KEY.isBlank()) {
        noOpRoutingDataSource
    } else {
        googleTransitDataSource
    }

    @Provides
    @Singleton
    fun provideRoutingJson(): Json = Json {
        ignoreUnknownKeys = true
    }

    @Provides
    @Singleton
    fun provideRoutingHttpClient(): OkHttpClient = OkHttpClient.Builder().build()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RoutingBindingsModule {
    @Binds
    abstract fun bindRoutingRepository(
        impl: RoutingRepositoryImpl,
    ): RoutingRepository
}
