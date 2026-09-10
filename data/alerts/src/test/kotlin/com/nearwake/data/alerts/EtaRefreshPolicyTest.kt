package com.nearwake.data.alerts

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class EtaRefreshPolicyTest {
    @Test
    fun `five second location samples cannot cause five second route requests`() {
        val policy = EtaRefreshPolicy().apply { reset("trip") }
        val attempts = (0L..120_000L step 5_000L).count {
            policy.shouldRefresh("trip", it, false)
        }
        assertThat(attempts).isEqualTo(3)
    }

    @Test
    fun `battery conservation halves the request frequency`() {
        val policy = EtaRefreshPolicy().apply { reset("trip") }
        assertThat(policy.shouldRefresh("trip", 0, true)).isTrue()
        assertThat(policy.shouldRefresh("trip", 60_000, true)).isFalse()
        assertThat(policy.shouldRefresh("trip", 120_000, true)).isTrue()
    }

    @Test
    fun `failed and expired estimates are never returned as current`() {
        val policy = EtaRefreshPolicy().apply { reset("trip") }
        policy.record("trip", 0, Result.success(5))
        assertThat(policy.currentEta("trip", 150_001)).isNull()
        policy.record("trip", 160_000, Result.success(3))
        policy.record("trip", 161_000, Result.failure(IllegalStateException("Offline")))
        assertThat(policy.currentEta("trip", 161_000)).isNull()
    }

    @Test
    fun `unknown eta is distinct from a failed or expired eta`() {
        val policy = EtaRefreshPolicy().apply { reset("trip") }
        assertThat(policy.isUnavailable("trip", 0)).isFalse()

        policy.record("trip", 0, Result.success(5))
        assertThat(policy.isUnavailable("trip", 150_000)).isFalse()
        assertThat(policy.isUnavailable("trip", 150_001)).isTrue()
    }

    @Test
    fun `retry backoff grows and is bounded`() {
        val policy = EtaRefreshPolicy().apply { reset("trip") }
        repeat(5) { policy.record("trip", 0, Result.failure(IllegalStateException())) }
        assertThat(policy.shouldRefresh("trip", 299_999, false)).isFalse()
        assertThat(policy.shouldRefresh("trip", 300_000, false)).isTrue()
    }

    @Test
    fun `old trip responses cannot contaminate a new trip`() {
        val policy = EtaRefreshPolicy().apply { reset("new") }
        policy.record("old", 0, Result.success(1))
        assertThat(policy.currentEta("new", 0)).isNull()
        assertThat(policy.shouldRefresh("old", 0, false)).isFalse()
    }
}
