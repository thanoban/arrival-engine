package com.nearwake.data.location

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.nearwake.core.common.region.RegionResolver
import com.nearwake.core.common.region.RegionSignalDataSource
import com.nearwake.core.common.region.RegionSignals
import com.nearwake.core.common.region.RegionSource
import com.nearwake.core.datastore.UserPreferencesDataStore
import java.io.File
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class ResolvedRegionRepositoryImplTest {
    @TempDir
    lateinit var tempDir: File

    @Test
    fun `resolved region uses locale when override and sim are unavailable`() = runTest {
        val repository = repository(signals = RegionSignals(simCountryCode = null, localeCountryCode = "lk"))

        val region = repository.observeResolvedRegion().first()

        assertEquals("LK", region.countryCode)
        assertEquals(RegionSource.LOCALE, region.source)
    }

    @Test
    fun `resolved region prefers sim over locale`() = runTest {
        val repository = repository(signals = RegionSignals(simCountryCode = "sg", localeCountryCode = "lk"))

        val region = repository.observeResolvedRegion().first()

        assertEquals("SG", region.countryCode)
        assertEquals(RegionSource.SIM, region.source)
    }

    @Test
    fun `resolved region prefers user override over sim and locale`() = runTest {
        val store = userPreferencesDataStore()
        store.setRegionOverrideCountryCode("in")
        val repository = repository(
            store = store,
            signals = RegionSignals(simCountryCode = "sg", localeCountryCode = "lk"),
        )

        val region = repository.observeResolvedRegion().first()

        assertEquals("IN", region.countryCode)
        assertEquals(RegionSource.USER_OVERRIDE, region.source)
    }

    @Test
    fun `clearing override returns resolved region to automatic signals`() = runTest {
        val store = userPreferencesDataStore()
        store.setRegionOverrideCountryCode("IN")
        val signalDataSource = FakeRegionSignalDataSource(
            RegionSignals(simCountryCode = "sg", localeCountryCode = "lk"),
        )
        val repository = repository(store = store, signalDataSource = signalDataSource)

        assertEquals(RegionSource.USER_OVERRIDE, repository.observeResolvedRegion().first().source)

        store.setRegionOverrideCountryCode(null)

        val region = repository.observeResolvedRegion().first()
        assertEquals("SG", region.countryCode)
        assertEquals(RegionSource.SIM, region.source)
    }

    private fun TestScope.repository(
        store: UserPreferencesDataStore = userPreferencesDataStore(),
        signals: RegionSignals,
    ): ResolvedRegionRepositoryImpl =
        repository(store = store, signalDataSource = FakeRegionSignalDataSource(signals))

    private fun repository(
        store: UserPreferencesDataStore,
        signalDataSource: RegionSignalDataSource,
    ): ResolvedRegionRepositoryImpl =
        ResolvedRegionRepositoryImpl(
            userPreferencesDataStore = store,
            regionSignalDataSource = signalDataSource,
            regionResolver = RegionResolver(),
        )

    private fun TestScope.userPreferencesDataStore(): UserPreferencesDataStore =
        UserPreferencesDataStore(
            PreferenceDataStoreFactory.create(
                scope = backgroundScope,
                produceFile = { File(tempDir, "user_preferences.preferences_pb") },
            ),
        )

    private class FakeRegionSignalDataSource(
        initialSignals: RegionSignals,
    ) : RegionSignalDataSource {
        private val signals = MutableStateFlow(initialSignals)

        override fun observeRegionSignals(): Flow<RegionSignals> = signals
    }
}
