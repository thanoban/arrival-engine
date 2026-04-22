package com.nearwake.data.routing.di

import com.nearwake.data.routing.NoOpRoutingDataSource
import com.nearwake.data.routing.RoutingRepositoryImpl
import com.nearwake.domain.routing.RoutingDataSource
import com.nearwake.domain.routing.repository.RoutingRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RoutingModule {
    @Binds
    abstract fun bindRoutingDataSource(
        impl: NoOpRoutingDataSource,
    ): RoutingDataSource

    @Binds
    abstract fun bindRoutingRepository(
        impl: RoutingRepositoryImpl,
    ): RoutingRepository
}
