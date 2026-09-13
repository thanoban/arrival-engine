package com.nearwake.data.location

import com.nearwake.core.common.region.RegionResolver
import com.nearwake.core.common.region.RegionSignalDataSource
import com.nearwake.core.common.region.ResolvedRegion
import com.nearwake.core.common.region.ResolvedRegionRepository
import com.nearwake.core.datastore.UserPreferencesDataStore
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged

@Singleton
class ResolvedRegionRepositoryImpl @Inject constructor(
    private val userPreferencesDataStore: UserPreferencesDataStore,
    private val regionSignalDataSource: RegionSignalDataSource,
    private val regionResolver: RegionResolver,
) : ResolvedRegionRepository {
    override fun observeResolvedRegion(): Flow<ResolvedRegion> =
        combine(
            userPreferencesDataStore.preferences,
            regionSignalDataSource.observeRegionSignals(),
        ) { preferences, signals ->
            regionResolver.resolve(
                userOverride = preferences.regionOverrideCountryCode,
                simCountryCode = signals.simCountryCode,
                localeCountryCode = signals.localeCountryCode,
            )
        }.distinctUntilChanged()
}
