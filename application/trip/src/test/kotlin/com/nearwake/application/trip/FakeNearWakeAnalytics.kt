package com.nearwake.application.trip

import com.nearwake.ports.analytics.NearWakeAnalytics

class FakeNearWakeAnalytics : NearWakeAnalytics {
    override fun trackTripArmed(
        mode: String,
        hasTransfers: Boolean,
        transferCount: Int,
    ) = Unit

    override fun trackAlertStageAdvanced(
        fromStage: String,
        toStage: String,
        confidence: String,
        distanceMeters: Double?,
        etaMinutes: Int?,
    ) = Unit

    override fun trackAlertFired(
        mode: String,
        confidence: String?,
        distanceMeters: Double?,
    ) = Unit

    override fun trackTripCompleted(
        stageReached: String,
        durationMinutes: Int?,
        alertCount: Int?,
    ) = Unit

    override fun trackRearmTapped() = Unit

    override fun trackTransferMissed(
        legIndex: Int,
        confidence: String,
    ) = Unit

    override fun recordFailure(
        surface: String,
        throwable: Throwable,
        attributes: Map<String, Any?>,
    ) = Unit
}
