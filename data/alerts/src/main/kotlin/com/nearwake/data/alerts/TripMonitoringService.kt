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
import com.nearwake.domain.trip.engine.TripEngineResult
import com.nearwake.domain.trip.model.MonitoringMode
import com.nearwake.domain.trip.model.TripSession
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@AndroidEntryPoint
class TripMonitoringService : Service() {
    @Inject lateinit var notificationHelper: NotificationHelper
    @Inject lateinit var tripCleanupUseCase: TripCleanupUseCase
    @Inject lateinit var diagnosticsLogger: DiagnosticsLogger
    @Inject lateinit var tripEngine: TripEngine
    @Inject lateinit var tripSessionStore: TripSessionStore

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
                serviceScope.launch {
                    val restoredSession = tripSessionStore.loadOrCreate(tripId)
                    activeSession = restoredSession
                    notificationHelper.notify(
                        NOTIFICATION_ID_MONITORING,
                        notificationHelper.buildMonitoringNotification(restoredSession.monitoringMode),
                    )
                    diagnosticsLogger.log(
                        eventType = "monitoring_service_started",
                        tripId = tripId,
                        payload = buildJsonObject {
                            put("state", restoredSession.state.name)
                            put("mode", restoredSession.monitoringMode.name)
                        },
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
                    persistSession(result)
                    notificationHelper.notify(
                        NOTIFICATION_ID_MONITORING,
                        notificationHelper.buildMonitoringNotification(result.session.monitoringMode),
                    )
                    diagnosticsLogger.log(
                        eventType = "trip_state_transition",
                        tripId = session.tripId,
                        payload = buildJsonObject {
                            put("event", "ApproachGeofenceEntered")
                            put("from_state", session.state.name)
                            put("to_state", result.session.state.name)
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
                persistSession(result)
                notificationHelper.notify(
                    NOTIFICATION_ID_MONITORING,
                    notificationHelper.buildMonitoringNotification(result.session.monitoringMode),
                )
                diagnosticsLogger.log(
                    eventType = "trip_state_transition",
                    tripId = session.tripId,
                    payload = buildJsonObject {
                        put("event", "MotionDetected")
                        put("motion_type", motion.type.name)
                        put("from_state", session.state.name)
                        put("to_state", result.session.state.name)
                    },
                )
            }
        }
    }

    private suspend fun persistSession(result: TripEngineResult) {
        tripSessionStore.save(result.session)
        activeSession = result.session
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
