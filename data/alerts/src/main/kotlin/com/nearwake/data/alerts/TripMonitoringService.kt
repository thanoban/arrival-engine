package com.nearwake.data.alerts

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.content.ContextCompat
import com.google.android.gms.location.Geofence
import com.nearwake.data.analytics.DiagnosticsLogger
import com.nearwake.data.location.receivers.GeofenceEventBus
import com.nearwake.data.motion.receivers.ActivityTransitionEventBus
import com.nearwake.domain.trip.engine.TripEngine
import com.nearwake.domain.trip.engine.TripEvent
import com.nearwake.domain.trip.model.MonitoringMode
import com.nearwake.domain.trip.model.TripSession
import com.nearwake.domain.trip.model.TripState
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@AndroidEntryPoint
class TripMonitoringService : Service() {
    @Inject lateinit var notificationHelper: NotificationHelper
    @Inject lateinit var tripCleanupUseCase: TripCleanupUseCase
    @Inject lateinit var diagnosticsLogger: DiagnosticsLogger
    @Inject lateinit var tripEngine: TripEngine

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var activeSession: TripSession? = null

    override fun onCreate() {
        super.onCreate()
        notificationHelper.ensureChannels()
        observeSignals()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_MONITORING -> {
                startForeground(
                    NOTIFICATION_ID_MONITORING,
                    notificationHelper.buildMonitoringNotification(MonitoringMode.GEOFENCE_ONLY),
                )
                val tripId = intent.getStringExtra(EXTRA_TRIP_ID) ?: return START_STICKY
                activeSession = TripSession(
                    tripId = tripId,
                    state = TripState.Armed,
                    monitoringMode = MonitoringMode.GEOFENCE_ONLY,
                    updatedAt = Clock.System.now(),
                )
                serviceScope.launch {
                    diagnosticsLogger.log(
                        eventType = "trip_started",
                        tripId = tripId,
                        payload = buildJsonObject {},
                    )
                }
            }

            ACTION_STOP_MONITORING -> stopSelf()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        runBlocking {
            tripCleanupUseCase()
        }
        stopForeground(STOP_FOREGROUND_REMOVE)
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun observeSignals() {
        serviceScope.launch {
            GeofenceEventBus.events.collect { event ->
                val session = activeSession ?: return@collect
                if (event.transitionType == Geofence.GEOFENCE_TRANSITION_ENTER) {
                    val result = tripEngine.onEvent(session, TripEvent.ApproachGeofenceEntered)
                    activeSession = result.session
                    notificationHelper.notify(
                        NOTIFICATION_ID_MONITORING,
                        notificationHelper.buildMonitoringNotification(result.session.monitoringMode),
                    )
                    diagnosticsLogger.log(
                        eventType = "geofence_fired",
                        tripId = session.tripId,
                        payload = buildJsonObject {
                            put("ids", event.geofenceIds.joinToString(","))
                        },
                    )
                }
            }
        }

        serviceScope.launch {
            ActivityTransitionEventBus.events.collect { motion ->
                val session = activeSession ?: return@collect
                val result = tripEngine.onEvent(session, TripEvent.MotionDetected)
                activeSession = result.session
                notificationHelper.notify(
                    NOTIFICATION_ID_MONITORING,
                    notificationHelper.buildMonitoringNotification(result.session.monitoringMode),
                )
                diagnosticsLogger.log(
                    eventType = "motion_transition",
                    tripId = session.tripId,
                    payload = buildJsonObject {
                        put("type", motion.type.name)
                    },
                )
            }
        }
    }

    companion object {
        private const val NOTIFICATION_ID_MONITORING = 41
        const val ACTION_START_MONITORING = "com.nearwake.data.alerts.START_MONITORING"
        const val ACTION_STOP_MONITORING = "com.nearwake.data.alerts.STOP_MONITORING"
        const val EXTRA_TRIP_ID = "extra_trip_id"

        fun start(context: Context, tripId: String) {
            ContextCompat.startForegroundService(
                context,
                Intent(context, TripMonitoringService::class.java).apply {
                    action = ACTION_START_MONITORING
                    putExtra(EXTRA_TRIP_ID, tripId)
                },
            )
        }

        fun stop(context: Context) {
            context.startService(
                Intent(context, TripMonitoringService::class.java).apply {
                    action = ACTION_STOP_MONITORING
                },
            )
        }
    }
}
