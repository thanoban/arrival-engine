package com.nearwake.data.analytics

import com.nearwake.ports.analytics.NearWakeAnalytics
import io.sentry.Breadcrumb
import io.sentry.Sentry
import io.sentry.SentryLevel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SentryNearWakeAnalytics @Inject constructor() : NearWakeAnalytics {
    override fun trackTripArmed(
        mode: String,
        hasTransfers: Boolean,
        transferCount: Int,
    ) {
        track(
            eventName = "trip_armed",
            properties = mapOf(
                "mode" to mode,
                "has_transfers" to hasTransfers,
                "transfer_count" to transferCount,
            ),
        )
    }

    override fun trackAlertStageAdvanced(
        fromStage: String,
        toStage: String,
        confidence: String,
        distanceMeters: Double?,
        etaMinutes: Int?,
    ) {
        track(
            eventName = "alert_stage_advanced",
            properties = mapOf(
                "from_stage" to fromStage,
                "to_stage" to toStage,
                "confidence" to confidence,
                "distance_m" to distanceMeters,
                "eta_min" to etaMinutes,
            ),
        )
    }

    override fun trackAlertFired(
        mode: String,
        confidence: String?,
        distanceMeters: Double?,
    ) {
        track(
            eventName = "alert_fired",
            properties = mapOf(
                "mode" to mode,
                "confidence" to confidence,
                "distance_m" to distanceMeters,
            ),
        )
    }

    override fun trackTripCompleted(
        stageReached: String,
        durationMinutes: Int?,
        alertCount: Int?,
    ) {
        track(
            eventName = "trip_completed",
            properties = mapOf(
                "stage_reached" to stageReached,
                "duration_min" to durationMinutes,
                "alert_count" to alertCount,
            ),
        )
    }

    override fun trackRearmTapped() {
        track(eventName = "rearm_tapped")
    }

    override fun trackTransferMissed(
        legIndex: Int,
        confidence: String,
    ) {
        track(
            eventName = "transfer_missed",
            properties = mapOf(
                "leg_index" to legIndex,
                "confidence" to confidence,
            ),
        )
    }

    override fun recordFailure(
        surface: String,
        throwable: Throwable,
        attributes: Map<String, Any?>,
    ) {
        addBreadcrumb(
            category = "failure",
            message = surface,
            level = SentryLevel.ERROR,
            properties = attributes,
        )
        Sentry.withScope { scope ->
            scope.setTag("failure_surface", surface)
            attributes.forEach { (key, value) ->
                value?.let { scope.setExtra(key, it.toString()) }
            }
            Sentry.captureException(throwable)
        }
    }

    private fun track(
        eventName: String,
        properties: Map<String, Any?> = emptyMap(),
    ) {
        addBreadcrumb(
            category = "analytics",
            message = eventName,
            level = SentryLevel.INFO,
            properties = properties,
        )
        Sentry.withScope { scope ->
            scope.setTag("analytics_event", eventName)
            properties.forEach { (key, value) ->
                value?.let { scope.setExtra(key, it.toString()) }
            }
            Sentry.captureMessage("analytics:$eventName", SentryLevel.INFO)
        }
    }

    private fun addBreadcrumb(
        category: String,
        message: String,
        level: SentryLevel,
        properties: Map<String, Any?>,
    ) {
        Sentry.addBreadcrumb(
            Breadcrumb().apply {
                this.category = category
                this.message = message
                this.level = level
                properties.forEach { (key, value) ->
                    value?.let { setData(key, it) }
                }
            },
        )
    }
}
