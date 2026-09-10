package com.nearwake.data.alerts

import com.google.common.truth.Truth.assertThat
import com.nearwake.core.remoteconfig.ThresholdConfig
import com.nearwake.domain.location.model.LatLng
import com.nearwake.domain.trip.engine.TripEngine
import com.nearwake.domain.trip.model.AlertIntensity
import com.nearwake.domain.trip.model.AlertStage
import com.nearwake.domain.trip.model.AlertTriggerMode
import com.nearwake.domain.trip.model.Confidence
import com.nearwake.domain.trip.model.MonitoringMode
import com.nearwake.domain.trip.model.TripSession
import com.nearwake.domain.trip.model.TripState
import kotlinx.datetime.Clock
import org.junit.jupiter.api.Test

class TripMonitoringRuntimeTest {
    private val runtime = TripMonitoringRuntime(
        tripEngine = TripEngine(),
        thresholdConfig = ThresholdConfig(),
    )

    private fun context() = runtime.buildContext(
        tripId = "trip-1", alertLeadMinutes = 5,
        alertIntensity = AlertIntensity.STANDARD,
        destination = LatLng(6.9271, 79.8612), hasCachedRoute = false,
    )

    @Test
    fun `delayed geofence from a different trip cannot fire arrival`() {
        assertThat(runtime.classifySignal(context(), listOf("old-trip:destination"))).isNull()
        assertThat(runtime.classifySignal(context(), listOf("trip-1:destination")))
            .isEqualTo(GeofenceSignal.DESTINATION)
    }

    @Test
    fun `first low power sample inside destination fires without waiting for another fix`() {
        val context = context()
        val session = TripSession(tripId = "trip-1", state = TripState.MonitoringLowPower,
            monitoringMode = MonitoringMode.BALANCED, updatedAt = Clock.System.now())
        val update = runtime.applyLocationUpdate(context, session, context.destination, null)
        assertThat(update.session.state).isEqualTo(TripState.Alerting)
        assertThat(update.engineResult!!.transition.sideEffects)
            .contains(com.nearwake.domain.trip.engine.TripSideEffect.FireArrivalAlert)
    }

    @Test
    fun `missing fresh eta does not revive a persisted estimate`() {
        val session = TripSession(tripId = "trip-1", state = TripState.MonitoringLowPower,
            monitoringMode = MonitoringMode.BALANCED, lastEtaMinutes = 1, updatedAt = Clock.System.now())
        val update = runtime.applyLocationUpdate(context(), session, LatLng(7.2, 80.2), null)
        assertThat(update.session.lastEtaMinutes).isNull()
        assertThat(update.session.state).isEqualTo(TripState.MonitoringLowPower)
    }

    @Test
    fun `buildGeofences creates approach and destination zones`() {
        val context = runtime.buildContext(
            tripId = "trip-1",
            alertLeadMinutes = 10,
            alertTriggerMode = AlertTriggerMode.TIME,
            alertIntensity = AlertIntensity.STANDARD,
            destination = LatLng(lat = 6.9271, lng = 79.8612),
            hasCachedRoute = true,
            initialEtaMinutes = 22,
        )

        val geofences = runtime.buildGeofences(context)

        assertThat(geofences).hasSize(2)
        assertThat(geofences[0].id).isEqualTo("trip-1:approach")
        assertThat(geofences[0].radiusMeters).isEqualTo(1_500f)
        assertThat(geofences[1].id).isEqualTo("trip-1:destination")
        assertThat(geofences[1].radiusMeters).isEqualTo(300f)
    }

    @Test
    fun `distance-only triggers use configured geofence radius`() {
        val context = runtime.buildContext(
            tripId = "trip-distance",
            alertLeadMinutes = 10,
            alertTriggerMode = AlertTriggerMode.DISTANCE,
            alertDistanceMeters = 1000,
            alertIntensity = AlertIntensity.STANDARD,
            destination = LatLng(lat = 6.9271, lng = 79.8612),
            hasCachedRoute = true,
        )

        val geofences = runtime.buildGeofences(context)

        assertThat(geofences[0].radiusMeters).isEqualTo(1000f)
    }

    @Test
    fun `applyLocationUpdate escalates low power session into approach mode`() {
        val context = runtime.buildContext(
            tripId = "trip-2",
            alertLeadMinutes = 10,
            alertTriggerMode = AlertTriggerMode.TIME,
            alertIntensity = AlertIntensity.STANDARD,
            destination = LatLng(lat = 6.9271, lng = 79.8612),
            hasCachedRoute = true,
        )
        val session = TripSession(
            tripId = "trip-2",
            state = TripState.MonitoringLowPower,
            monitoringMode = MonitoringMode.BALANCED,
            confidence = Confidence.HIGH,
            updatedAt = Clock.System.now(),
        )

        val update = runtime.applyLocationUpdate(
            context = context,
            session = session,
            location = LatLng(lat = 6.9000, lng = 79.8300),
            etaMinutes = 8,
        )

        assertThat(update.engineResult).isNotNull()
        assertThat(update.session.state).isEqualTo(TripState.MonitoringApproach)
        assertThat(update.session.monitoringMode).isEqualTo(MonitoringMode.PRECISE_BURST)
        assertThat(update.session.alertStage).isEqualTo(AlertStage.APPROACH)
        assertThat(update.session.lastKnownLat).isWithin(0.000001).of(6.9000)
        assertThat(update.session.lastEtaMinutes).isEqualTo(8)
    }

    @Test
    fun `applyLocationUpdate fires alert when destination radius is reached`() {
        val context = runtime.buildContext(
            tripId = "trip-3",
            alertLeadMinutes = 5,
            alertTriggerMode = AlertTriggerMode.TIME,
            alertIntensity = AlertIntensity.LOUD,
            destination = LatLng(lat = 6.9271, lng = 79.8612),
            hasCachedRoute = true,
        )
        val session = TripSession(
            tripId = "trip-3",
            state = TripState.MonitoringApproach,
            monitoringMode = MonitoringMode.PRECISE_BURST,
            confidence = Confidence.HIGH,
            updatedAt = Clock.System.now(),
        )

        val update = runtime.applyLocationUpdate(
            context = context,
            session = session,
            location = LatLng(lat = 6.927101, lng = 79.861201),
            etaMinutes = 1,
        )

        assertThat(update.engineResult).isNotNull()
        assertThat(update.session.state).isEqualTo(TripState.Alerting)
        assertThat(update.session.monitoringMode).isEqualTo(MonitoringMode.GEOFENCE_ONLY)
        assertThat(update.session.alertStage).isEqualTo(AlertStage.ARRIVAL)
        assertThat(update.distanceMeters).isLessThan(300.0)
    }
}
