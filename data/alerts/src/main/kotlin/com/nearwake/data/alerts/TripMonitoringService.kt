package com.nearwake.data.alerts

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.content.ContextCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.nearwake.core.database.dao.SavedPlaceDao
import com.nearwake.core.database.dao.TripDao
import com.nearwake.data.analytics.DiagnosticsLogger
import com.nearwake.data.location.GeofenceDataSource
import com.nearwake.data.location.LocationStrategyOrchestrator
import com.nearwake.data.location.receivers.GeofenceEventBus
import com.nearwake.data.motion.ActivityRecognitionDataSource
import com.nearwake.data.motion.receivers.ActivityTransitionEventBus
import com.nearwake.domain.location.model.LatLng
import com.nearwake.domain.routing.repository.RoutingRepository
import com.nearwake.domain.trip.engine.TripEngine
import com.nearwake.domain.trip.engine.TripEngineResult
import com.nearwake.domain.trip.engine.TripEvent
import com.nearwake.domain.trip.engine.TripSideEffect
import com.nearwake.domain.trip.model.Confidence
import com.nearwake.domain.trip.model.AlertStage
import com.nearwake.domain.trip.model.MonitoringMode
import com.nearwake.domain.trip.model.TripSession
import com.nearwake.domain.trip.model.TripState
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
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
    @Inject lateinit var tripSessionStore: TripSessionStore
    @Inject lateinit var tripMonitoringRuntime: TripMonitoringRuntime
    @Inject lateinit var tripDao: TripDao
    @Inject lateinit var savedPlaceDao: SavedPlaceDao
    @Inject lateinit var routingRepository: RoutingRepository
    @Inject lateinit var geofenceDataSource: GeofenceDataSource
    @Inject lateinit var activityRecognitionDataSource: ActivityRecognitionDataSource
    @Inject lateinit var locationStrategyOrchestrator: LocationStrategyOrchestrator
    @Inject lateinit var alertOrchestrator: AlertOrchestrator

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var activeSession: TripSession? = null
    private var activeContext: MonitoredTripContext? = null
    private var locationJob: Job? = null

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
                    notificationHelper.buildMonitoringNotification(
                        mode = MonitoringMode.GEOFENCE_ONLY,
                        stage = AlertStage.MONITORING,
                    ),
                )
                val tripId = intent.getStringExtra(EXTRA_TRIP_ID) ?: return START_STICKY
                serviceScope.launch {
                    startMonitoring(tripId)
                }
            }

            ACTION_STOP_MONITORING -> stopSelf()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        runBlocking {
            locationJob?.cancel()
            tripCleanupUseCase()
        }
        stopForeground(STOP_FOREGROUND_REMOVE)
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private suspend fun startMonitoring(tripId: String) {
        val context = loadTripContext(tripId)
        if (context == null) {
            diagnosticsLogger.log(
                eventType = "monitoring_service_missing_trip_context",
                tripId = tripId,
                payload = buildJsonObject {},
            )
            stopSelf()
            return
        }

        activeContext = context
        val restoredSession = tripSessionStore.loadOrCreate(tripId)
            .withInitialEta(context.initialEtaMinutes)
        val preparedSession = registerPrerequisites(restoredSession, context)
        persistSession(preparedSession)

        val restorationResult = tripEngine.restoreSession(preparedSession)
        applyEngineResult(
            result = restorationResult,
            eventName = "RestoreMonitoring",
            context = context,
        )
        diagnosticsLogger.log(
            eventType = "monitoring_service_started",
            tripId = tripId,
            payload = buildJsonObject {
                put("state", restorationResult.session.state.name)
                put("mode", restorationResult.session.monitoringMode.name)
                put("stage", restorationResult.session.alertStage.name)
                put("route_cached", context.hasCachedRoute)
            },
        )
    }

    private fun observeSignals() {
        serviceScope.launch {
            GeofenceEventBus.events.collect { event ->
                val context = activeContext ?: return@collect
                val session = activeSession ?: return@collect

                if (event.errorCode == GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE) {
                    diagnosticsLogger.log(
                        eventType = "geofence_not_available",
                        tripId = session.tripId,
                        payload = buildJsonObject {},
                    )
                    startTracking(MonitoringMode.BALANCED)
                    return@collect
                }

                if (event.transitionType != Geofence.GEOFENCE_TRANSITION_ENTER) {
                    return@collect
                }

                when (tripMonitoringRuntime.classifySignal(event.geofenceIds)) {
                    GeofenceSignal.APPROACH -> applyEngineResult(
                        result = tripEngine.onEvent(session, TripEvent.ApproachGeofenceEntered),
                        eventName = "ApproachGeofenceEntered",
                        context = context,
                        extraPayload = buildJsonObject {
                            put("ids", event.geofenceIds.joinToString(","))
                        },
                    )

                    GeofenceSignal.DESTINATION -> handleDestinationReached(context)
                    null -> Unit
                }
            }
        }

        serviceScope.launch {
            ActivityTransitionEventBus.events.collect { motion ->
                val context = activeContext ?: return@collect
                val session = activeSession ?: return@collect
                applyEngineResult(
                    result = tripEngine.onEvent(session, TripEvent.MotionDetected),
                    eventName = "MotionDetected",
                    context = context,
                    extraPayload = buildJsonObject {
                        put("motion_type", motion.type.name)
                    },
                )
            }
        }
    }

    private suspend fun handleDestinationReached(context: MonitoredTripContext) {
        val session = activeSession ?: return
        val location = context.destination
        val etaMinutes = session.lastEtaMinutes ?: context.initialEtaMinutes
        val update = tripMonitoringRuntime.applyLocationUpdate(
            context = context,
            session = session,
            location = location,
            etaMinutes = etaMinutes,
            destinationGeofenceEntered = true,
        )
        applyLocationUpdate(
            update = update,
            eventName = "DestinationGeofenceEntered",
            context = context,
        )
    }

    private suspend fun registerPrerequisites(
        session: TripSession,
        context: MonitoredTripContext,
    ): TripSession {
        val geofences = tripMonitoringRuntime.buildGeofences(context)
        geofenceDataSource.removeGeofences(context.geofenceIds)
        geofenceDataSource.registerGeofences(geofences)
            .onFailure { error ->
                diagnosticsLogger.log(
                    eventType = "geofence_registration_failed",
                    tripId = session.tripId,
                    payload = buildJsonObject {
                        put("message", error.message.orEmpty())
                    },
                )
            }
            .getOrThrow()

        runCatching {
            activityRecognitionDataSource.registerVehicleTransitions()
        }.onFailure { error ->
            diagnosticsLogger.log(
                eventType = "activity_transition_registration_failed",
                tripId = session.tripId,
                payload = buildJsonObject {
                    put("message", error.message.orEmpty())
                },
            )
        }

        return session.copy(geofenceIds = context.geofenceIds)
    }

    private suspend fun startTracking(mode: MonitoringMode) {
        locationJob?.cancel()
        if (mode == MonitoringMode.GEOFENCE_ONLY) {
            locationStrategyOrchestrator.stop()
            return
        }

        val context = activeContext ?: return
        val updates = locationStrategyOrchestrator.escalate(mode)
        locationJob = serviceScope.launch {
            updates.collectLatest { location ->
                handleLocationUpdate(context, location)
            }
        }
    }

    private suspend fun handleLocationUpdate(
        context: MonitoredTripContext,
        location: LatLng,
    ) {
        var session = activeSession ?: return
        val etaResult = if (context.hasCachedRoute) {
            routingRepository.refreshEta(context.tripId, location)
        } else {
            Result.failure(IllegalStateException("No cached route is available for this trip."))
        }

        if (context.hasCachedRoute) {
            session = when {
                etaResult.isSuccess && session.confidence == Confidence.OFFLINE -> {
                    val result = tripEngine.onEvent(session, TripEvent.NetworkRestored())
                    applyEngineResult(
                        result = result,
                        eventName = "NetworkRestored",
                        context = context,
                    )
                    activeSession ?: result.session
                }

                etaResult.isFailure && session.confidence != Confidence.OFFLINE -> {
                    val result = tripEngine.onEvent(session, TripEvent.NetworkLost)
                    applyEngineResult(
                        result = result,
                        eventName = "NetworkLost",
                        context = context,
                        extraPayload = buildJsonObject {
                            put("message", etaResult.exceptionOrNull()?.message.orEmpty())
                        },
                    )
                    activeSession ?: result.session
                }

                else -> session
            }
        }

        val update = tripMonitoringRuntime.applyLocationUpdate(
            context = context,
            session = session,
            location = location,
            etaMinutes = etaResult.getOrNull() ?: session.lastEtaMinutes,
        )
        applyLocationUpdate(
            update = update,
            eventName = "LocationUpdate",
            context = context,
        )
    }

    private suspend fun applyLocationUpdate(
        update: TripMonitoringUpdate,
        eventName: String,
        context: MonitoredTripContext,
    ) {
        if (update.engineResult == null) {
            val previousSession = activeSession
            persistSession(update.session)
            refreshMonitoringNotification(
                mode = update.session.monitoringMode,
                stage = update.session.alertStage,
            )
            maybeNotifyStageAdvance(
                previousSession = previousSession,
                currentSession = update.session,
                context = context,
            )
            diagnosticsLogger.log(
                eventType = "trip_location_sampled",
                tripId = update.session.tripId,
                payload = buildJsonObject {
                    put("event", eventName)
                    put("distance_meters", update.distanceMeters)
                    put("eta_minutes", update.etaMinutes ?: -1)
                    put("state", update.session.state.name)
                    put("stage", update.session.alertStage.name)
                },
            )
            return
        }

        applyEngineResult(
            result = update.engineResult,
            eventName = eventName,
            context = context,
            extraPayload = buildJsonObject {
                put("distance_meters", update.distanceMeters)
                put("eta_minutes", update.etaMinutes ?: -1)
            },
        )
    }

    private suspend fun applyEngineResult(
        result: TripEngineResult,
        eventName: String,
        context: MonitoredTripContext,
        extraPayload: kotlinx.serialization.json.JsonObject? = null,
    ) {
        val previousSession = activeSession
        persistSession(result.session)
        refreshMonitoringNotification(
            mode = result.session.monitoringMode,
            stage = result.session.alertStage,
        )
        maybeNotifyStageAdvance(
            previousSession = previousSession,
            currentSession = result.session,
            context = context,
        )
        diagnosticsLogger.log(
            eventType = "trip_state_transition",
            tripId = result.session.tripId,
            payload = buildJsonObject {
                put("event", eventName)
                put("from_state", result.transition.fromState.name)
                put("to_state", result.session.state.name)
                put("mode", result.session.monitoringMode.name)
                put("stage", result.session.alertStage.name)
                put("ignored", result.transition.ignored)
                extraPayload?.forEach { (key, value) -> put(key, value) }
            },
        )
        handleSideEffects(result, context)
    }

    private suspend fun handleSideEffects(
        result: TripEngineResult,
        context: MonitoredTripContext,
    ) {
        result.transition.sideEffects.forEach { sideEffect ->
            when (sideEffect) {
                TripSideEffect.StartBalancedTracking,
                TripSideEffect.StartRecoveryChecks,
                TripSideEffect.RestartMonitoring -> startTracking(MonitoringMode.BALANCED)

                TripSideEffect.StartPreciseBurst -> startTracking(MonitoringMode.PRECISE_BURST)

                TripSideEffect.StopLocationTracking -> {
                    locationJob?.cancel()
                    locationStrategyOrchestrator.stop()
                }

                TripSideEffect.FireArrivalAlert -> {
                    alertOrchestrator.fireAlert(
                        tripId = context.tripId,
                        intensity = context.alertIntensity,
                        mode = context.alertMode,
                    )
                }

                TripSideEffect.CleanupMonitoring -> {
                    tripCleanupUseCase()
                    stopSelf()
                }

                TripSideEffect.RestorePersistedMonitoring -> startTracking(result.session.monitoringMode)

                TripSideEffect.LogTripCompletion -> {
                    tripDao.getTripById(context.tripId)?.let { trip ->
                        tripDao.upsertTrip(trip.copy(completedAt = Clock.System.now()))
                    }
                }

                TripSideEffect.PersistSession,
                TripSideEffect.RegisterGeofences,
                TripSideEffect.RegisterActivityTransitions,
                TripSideEffect.PromptForMovement,
                TripSideEffect.ApplyOfflineBias,
                TripSideEffect.ClearOfflineBias -> Unit
            }
        }
    }

    private suspend fun loadTripContext(tripId: String): MonitoredTripContext? {
        val trip = tripDao.getTripById(tripId) ?: return null
        val destination = savedPlaceDao.getSavedPlaceById(trip.destinationId) ?: return null
        val cachedRoute = routingRepository.getCachedRoute(tripId)
        return tripMonitoringRuntime.buildContext(
            tripId = trip.id,
            destinationName = destination.name,
            alertLeadMinutes = trip.alertLeadMinutes,
            alertIntensity = trip.alertIntensity,
            alertMode = trip.alertMode,
            destination = LatLng(lat = destination.lat, lng = destination.lng),
            hasCachedRoute = cachedRoute != null,
            initialEtaMinutes = cachedRoute?.totalDurationMinutes,
        )
    }

    private suspend fun persistSession(session: TripSession) {
        tripSessionStore.save(session)
        activeSession = session
    }

    private fun refreshMonitoringNotification(
        mode: MonitoringMode,
        stage: AlertStage,
    ) {
        val session = activeSession
        val context = activeContext
        notificationHelper.notify(
            NOTIFICATION_ID_MONITORING,
            notificationHelper.buildMonitoringNotification(
                mode = mode,
                stage = stage,
                destinationName = context?.destinationName,
                etaMinutes = session?.lastEtaMinutes,
                confidence = session?.confidence,
                alertMode = context?.alertMode,
            ),
        )
    }

    private fun maybeNotifyStageAdvance(
        previousSession: TripSession?,
        currentSession: TripSession,
        context: MonitoredTripContext,
    ) {
        val previousStage = previousSession?.alertStage ?: AlertStage.MONITORING
        val currentStage = currentSession.alertStage
        if (currentStage.ordinal <= previousStage.ordinal) {
            return
        }
        if (currentStage == AlertStage.ARRIVAL || currentStage == AlertStage.RECOVERY) {
            return
        }
        notificationHelper.notify(
            NOTIFICATION_ID_STAGE,
            notificationHelper.buildStageNotification(
                tripId = context.tripId,
                destinationName = context.destinationName,
                stage = currentStage,
                mode = context.alertMode,
            ),
        )
    }

    private fun TripSession.withInitialEta(initialEtaMinutes: Int?): TripSession =
        if (lastEtaMinutes == null && initialEtaMinutes != null) {
            copy(lastEtaMinutes = initialEtaMinutes)
        } else {
            this
        }

    companion object {
        private const val NOTIFICATION_ID_MONITORING = 41
        private const val NOTIFICATION_ID_STAGE = 43
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
