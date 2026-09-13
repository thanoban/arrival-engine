package com.nearwake.core.common.region

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class RegionResolverTest {
    private val resolver = RegionResolver()

    @Test
    fun `user override takes precedence and is normalized`() {
        val region = resolver.resolve(
            userOverride = " lk ",
            simCountryCode = "IN",
            localeCountryCode = "GB",
        )

        assertEquals("LK", region.countryCode)
        assertEquals(RegionSource.USER_OVERRIDE, region.source)
    }

    @Test
    fun `sim country takes precedence over locale without override`() {
        val region = resolver.resolve(
            userOverride = null,
            simCountryCode = "in",
            localeCountryCode = "GB",
        )

        assertEquals("IN", region.countryCode)
        assertEquals(RegionSource.SIM, region.source)
    }

    @Test
    fun `invalid override falls back to locale when sim is unavailable`() {
        val region = resolver.resolve(
            userOverride = "Sri Lanka",
            simCountryCode = "",
            localeCountryCode = "lk",
        )

        assertEquals("LK", region.countryCode)
        assertEquals(RegionSource.LOCALE, region.source)
    }

    @Test
    fun `missing signals resolve to unspecified without inventing a country`() {
        val region = resolver.resolve(
            userOverride = null,
            simCountryCode = "1",
            localeCountryCode = "",
        )

        assertNull(region.countryCode)
        assertEquals(RegionSource.UNSPECIFIED, region.source)
    }
}
