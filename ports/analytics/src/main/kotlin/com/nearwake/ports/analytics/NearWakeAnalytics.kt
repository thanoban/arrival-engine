package com.nearwake.ports.analytics

interface NearWakeAnalytics {
    fun trackTripArmed(
        mode: String,
        hasTransfers: Boolean,
        transferCount: Int,
    )

    fun trackAlertStageAdvanced(
        fromStage: String,
        toStage: String,
        confidence: String,
        distanceMeters: Double?,
        etaMinutes: Int?,
    )

    fun trackAlertFired(
        mode: String,
        confidence: String?,
        distanceMeters: Double?,
    )

    fun trackTripCompleted(
        stageReached: String,
        durationMinutes: Int?,
        alertCount: Int?,
    )

    fun trackRearmTapped()

    fun trackTransferMissed(
        legIndex: Int,
        confidence: String,
    )

    fun recordFailure(
        surface: String,
        throwable: Throwable,
        attributes: Map<String, Any?> = emptyMap(),
    )
}
