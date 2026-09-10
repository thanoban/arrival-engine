package com.nearwake.data.alerts

/** In-memory freshness and request budget for the single actively monitored trip. */
internal class EtaRefreshPolicy {
    private var tripId: String? = null
    private var nextAttemptAt = 0L
    private var lastSuccessAt: Long? = null
    private var lastEta: Int? = null
    private var failures = 0
    private var hasCompletedAttempt = false

    fun reset(id: String) {
        tripId = id
        nextAttemptAt = 0L
        lastSuccessAt = null
        lastEta = null
        failures = 0
        hasCompletedAttempt = false
    }

    fun shouldRefresh(id: String, nowMs: Long, conserveBattery: Boolean): Boolean {
        if (tripId != id || nowMs < nextAttemptAt) return false
        nextAttemptAt = nowMs + if (conserveBattery) 120_000L else 60_000L
        return true
    }

    fun record(id: String, nowMs: Long, result: Result<Int>) {
        if (id != tripId) return
        hasCompletedAttempt = true
        val eta = result.getOrNull()?.takeIf { it >= 0 }
        if (eta != null) {
            failures = 0
            lastSuccessAt = nowMs
            lastEta = eta
        } else {
            failures = (failures + 1).coerceAtMost(4)
            nextAttemptAt = maxOf(nextAttemptAt, nowMs + minOf(300_000L, 30_000L shl failures))
            lastSuccessAt = null
            lastEta = null
        }
    }

    fun currentEta(id: String, nowMs: Long): Int? {
        val at = lastSuccessAt ?: return null
        return lastEta?.takeIf { id == tripId && nowMs - at in 0..MAX_ETA_AGE_MS }
    }

    fun isUnavailable(id: String, nowMs: Long): Boolean =
        id == tripId && hasCompletedAttempt && currentEta(id, nowMs) == null

    private companion object {
        const val MAX_ETA_AGE_MS = 150_000L
    }
}
