package com.nearwake.core.common.region

import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

enum class RegionSource {
    USER_OVERRIDE,
    SIM,
    LOCALE,
    UNSPECIFIED,
}

data class ResolvedRegion(
    val countryCode: String?,
    val source: RegionSource,
)

data class RegionSignals(
    val simCountryCode: String?,
    val localeCountryCode: String?,
)

interface RegionSignalDataSource {
    fun observeRegionSignals(): Flow<RegionSignals>
}

interface ResolvedRegionRepository {
    fun observeResolvedRegion(): Flow<ResolvedRegion>
}

class RegionResolver @Inject constructor() {
    fun resolve(
        userOverride: String?,
        simCountryCode: String?,
        localeCountryCode: String?,
    ): ResolvedRegion {
        normalizeCountryCode(userOverride)?.let {
            return ResolvedRegion(it, RegionSource.USER_OVERRIDE)
        }
        normalizeCountryCode(simCountryCode)?.let {
            return ResolvedRegion(it, RegionSource.SIM)
        }
        normalizeCountryCode(localeCountryCode)?.let {
            return ResolvedRegion(it, RegionSource.LOCALE)
        }
        return ResolvedRegion(countryCode = null, source = RegionSource.UNSPECIFIED)
    }

    private fun normalizeCountryCode(value: String?): String? =
        value
            ?.trim()
            ?.uppercase(Locale.ROOT)
            ?.takeIf { code -> code.length == ISO_COUNTRY_CODE_LENGTH && code.all { it in 'A'..'Z' } }

    private companion object {
        const val ISO_COUNTRY_CODE_LENGTH = 2
    }
}
