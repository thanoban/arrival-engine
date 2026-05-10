package com.nearwake.core.remoteconfig

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class StaticRemoteConfigRepositoryTest {
    @Test
    fun `returns the default threshold config`() {
        val config = StaticRemoteConfigRepository().currentThresholdConfig()

        assertThat(config.approachGeofenceRadiusM).isEqualTo(1_500f)
        assertThat(config.approachGeofenceBatterySaverM).isEqualTo(2_250f)
        assertThat(config.biasMultiplierDegradedActive).isEqualTo(1.15)
        assertThat(config.biasMultiplierDegradedSleep).isEqualTo(1.20)
        assertThat(config.biasMultiplierOffline).isEqualTo(1.25)
        assertThat(config.minTripsForClustering).isEqualTo(2)
    }
}
