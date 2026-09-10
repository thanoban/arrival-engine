package com.nearwake.data.alerts

import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.IBinder
import android.os.PowerManager
import android.os.SystemClock
import androidx.core.content.ContextCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.nearwake.application.trip.MarkTripCompletedUseCase
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
import com.nearwake.domain.trip.model.AlertStage
import com.nearwake.domain.trip.model.Confidence
import com.nearwake.domain.trip.model.MonitoringMode
import com.nearwake.domain.trip.model.TripSession
import com.nearwake.domain.trip.model.TripState
import com.nearwake.domain.trip.model.isTerminal
import com.nearwake.ports.analytics.NearWakeAnalytics
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@AndroidEntryPoint
class TripMonitoringService : Service() {
    @Inject lateinit var notificationHelper: NotificationHelper
    @Inject lateinit var tripCleanupUseCase: TripCleanupUseCase
    @Inject lateinit var diagnosticsLogger: DiagnosticsLogger
    @Inject lateinit var analytics: NearWakeAnalytics
    @Inject lateinit var tripEngine: TripEngine
    @Inject lateinit var tripSessionStore: TripSessionStore
    @Inject lateinit var tripMonitoringRuntime: TripMonitoringRuntime
    @Inject lateinit var markTripCompleted: MarkTripCompletedUseCase
    @Inject lateinit var routingRepository: RoutingRepository
    @Inject lateinit var geofenceDataSource: GeofenceDataSource
    @Inject lateinit var activityRecognitionDataSource: ActivityRecognitionDataSource
    @Inject lateinit var locationStrategyOrchestrator: LocationStrategyOrchestrator
    @Inject lateinit var alertOrchestrator: AlertOrchestrator
    @Inject lateinit var feedbackCoordinator: TripMonitoringFeedbackCoordinator
    @Inject lateinit var contextLoader: MonitoredTripContextLoader

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val stateMutex = Mutex()
    private var activeSession: TripSession? = null
    private var activeContext: MonitoredTripContext? = null
    private var locationJob: Job? = null
    private var etaRefreshJob: Job? = null
    private val etaPolicy = EtaRefreshPolicy()
    private val locationSamples = Channel<Pair<MonitoredTripContext, LatLng>>(Channel.CONFLATED)

    override fun onCreate() {
        super.onCreate()
        notificationHelper.ensureChannels()
        observeSignals()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action ?: ACTION_START_MONITORING) {
            ACTION_START_MONITORING -> {
                startForeground(
                    NOTIFICATION_ID_MONITORING,
                    notificationHelper.buildMonitoringNotification(
                        mode = MonitoringMode.GEOFENCE_ONLY,
                        stage = AlertStage.MONITORING,
                    ),
                )
                serviceScope.launch {
                    stateMutex.withLock {
                        val tripId = intent?.getStringExtra(EXTRA_TRIP_ID)
                            ?: tripSessionStore.loadActive()?.tripId
                        if (tripId == null) {
                            stopSelf(startId)
                            return@withLock
                        }
                        runCatching { startMonitoring(tripId) }
                            .onFailure { e ->
                                if (e is CancellationException) throw e
                                notificationHelper.notify(
                                    NOTIFICATION_ID_BOARDING_WARNING,
                                    notificationHelper.buildMonitoringProblemNotification(tripId),
                                )
                                diagnosticsLogger.log(
                                    eventType = "monitoring_start_failed",
                                    tripId = tripId,
                                    payload = buildJsonObject {
                                        put("message", e.message.orEmpty())
                                    },
                                )
                                stopSelf()
                            }
                    }
                }
            }

            ACTION_STOP_MONITORING -> stopSelf()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        locationJob?.cancel()
        etaRefreshJob?.cancel()
        locationSamples.close()
        feedbackCoordinator.reset()
        launchCleanup(activeSession?.tripId)
        stopForeground(STOP_FOREGROUND_REMOVE)
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private suspend fun startMonitoring(tripId: String) {
        locationJob?.cancel()
        etaRefreshJob?.cancel()
        etaPolicy.reset(tripId)
        val context = contextLoader.load(
            tripId = tripId,
            batterySaverMode = readBatteryPercent() < BATTERY_SAVER_THRESHOLD,
        )
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
        feedbackCoordinator.reset()
        val restoredSession = tripSessionStore.loadOrCreate(tripId)
            .withInitialEta(context.initialEtaMinutes)
        if (restoredSession.state.isTerminal) {
            stopSelf()
            return
        }
        val preparedSession = registerPrerequisites(restoredSession, context)
        persistSession(preparedSession)

        val restorationResult = tripEngine.restoreSession(preparedSession)
        applyEngineResult(
            result = restorationResult,
            eventName = "RestoreMonitoring",
            context = context,
        )
        if (restorationResult.session.state == TripState.Armed ||
            restorationResult.session.state == TripState.WaitingForMovement) {
            // Starting while already aboard may never produce an activity ENTER event.
            startTracking(MonitoringMode.BALANCED)
        }
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
        // Process decisions outside the producer job: switching GPS modes cancels that job.
        serviceScope.launch {
            for ((context, location) in locationSamples) {
                stateMutex.withLock {
                    if (activeContext !== context) return@withLock
                    try {
                        handleLocationUpdate(context, location)
                    } catch (cancelled: CancellationException) {
                        throw cancelled
                    } catch (error: Exception) {
                        failMonitoring(context, error)
                    }
                }
            }
        }
        serviceScope.launch {
            GeofenceEventBus.events.collect { event ->
                stateMutex.withLock {
                    val context = activeContext ?: return@withLock
                    val session = activeSession ?: return@withLock

                    if (event.errorCode == GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE) {
                        diagnosticsLogger.log(
                            eventType = "geofence_not_available",
                            tripId = session.tripId,
                            payload = buildJsonObject {},
                        )
                        startTracking(MonitoringMode.BALANCED)
                        return@withLock
                    }

                    if (event.transitionType != Geofence.GEOFENCE_TRANSITION_ENTER) {
                        return@withLock
                    }

                    when (tripMonitoringRuntime.classifySignal(context, event.geofenceIds)) {
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
        }

        serviceScope.launch {
            ActivityTransitionEventBus.events.collect { motion ->
                stateMutex.withLock {
                    val context = activeContext ?: return@withLock
                    val session = activeSession ?: return@withLock
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
                analytics.recordFailure(
                    surface = "geofence_registration",
                    throwable = error,
                    attributes = mapOf("trip_id" to session.tripId),
                )
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
            analytics.recordFailure(
                surface = "activity_transition_registration",
                throwable = error,
                attributes = mapOf("trip_id" to session.tripId),
            )
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
        val minDistance = if (mode == MonitoringMode.BALANCED && !isScreenOn()) {
            BALANCED_SCREEN_OFF_MIN_DISTANCE
        } else {
            BALANCED_SCREEN_ON_MIN_DISTANCE
        }
        val updates = locationStrategyOrchestrator.escalate(mode, balancedMinDistanceMeters = minDistance)
        locationJob = serviceScope.launch {
            updates.catch { error ->
                stateMutex.withLock {
                    if (activeContext === context) failMonitoring(context, error)
                }
            }.collect { location ->
                locationSamples.trySend(context to location)
            }
        }
    }

    private suspend fun failMonitoring(context: MonitoredTripContext, error: Throwable) {
        try {
            activeSession?.let {
                persistSession(
                    it.copy(
                        state = TripState.FailedGracefully,
                        monitoringMode = MonitoringMode.GEOFENCE_ONLY,
                        confidence = Confidence.OFFLINE,
                        lastEtaMinutes = null,
                        updatedAt = Clock.System.now(),
                    ),
                )
            }
            notificationHelper.notify(
                NOTIFICATION_ID_BOARDING_WARNING,
                notificationHelper.buildMonitoringProblemNotification(context.tripId),
            )
            analytics.recordFailure("location_monitoring", error, mapOf("trip_id" to context.tripId))
        } finally {
            stopSelf()
        }
    }

    private suspend fun handleLocationUpdate(
        context: MonitoredTripContext,
        location: LatLng,
    ) {
        var session = activeSession ?: return
        if (session.state.isTerminal || session.state == TripState.Alerting) return
        if (session.state == TripState.Armed || session.state == TripState.WaitingForMovement) {
            session = session.copy(state = TripState.MonitoringLowPower, monitoringMode = MonitoringMode.BALANCED)
        }
        val now = SystemClock.elapsedRealtime()
        val conserveBattery = isOnCellularOnly() && readBatteryPercent() < NETWORK_SUPPRESS_THRESHOLD
        if (context.hasCachedRoute && etaRefreshJob?.isActive != true &&
            etaPolicy.shouldRefresh(context.tripId, now, conserveBattery)) {
            etaRefreshJob = serviceScope.launch {
                val result = try {
                    routingRepository.refreshEta(context.tripId, location)
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (error: Exception) {
                    Result.failure(error)
                }
                stateMutex.withLock {
                    if (activeContext !== context) return@withLock
                    etaPolicy.record(context.tripId, SystemClock.elapsedRealtime(), result)
                }
                locationSamples.trySend(context to location)
            }
        }

        val freshEta = etaPolicy.currentEta(context.tripId, now)
        if (context.hasCachedRoute) {
            val confidenceEvent = when {
                freshEta != null && session.confidence == Confidence.OFFLINE -> TripEvent.NetworkRestored()
                etaPolicy.isUnavailable(context.tripId, now) && session.confidence != Confidence.OFFLINE -> {
                    TripEvent.NetworkLost
                }
                else -> null
            }
            if (confidenceEvent != null) {
                val result = tripEngine.onEvent(session, confidenceEvent)
                applyEngineResult(
                    result = result,
                    eventName = confidenceEvent::class.simpleName ?: "NetworkStatusChanged",
                    context = context,
                )
                session = activeSession ?: result.session
            }
        }
        session = session.copy(monitoringMode = locationStrategyOrchestrator.monitoringMode.value)

        val update = tripMonitoringRuntime.applyLocationUpdate(
            context = context,
            session = session,
            location = location,
            etaMinutes = freshEta,
        )
        applyLocationUpdate(
            update = update,
            eventName = "LocationUpdate",
            context = context,
            sampleLocation = location,
            previousSession = session,
        )
    }

    private suspend fun applyLocationUpdate(
        update: TripMonitoringUpdate,
        eventName: String,
        context: MonitoredTripContext,
        sampleLocation: LatLng? = null,
        previousSession: TripSession? = null,
    ) {
        if (update.engineResult == null) {
            val priorSession = previousSession ?: activeSession
            persistSession(update.session)
            feedbackCoordinator.refreshMonitoringNotification(
                session = update.session,
                context = context,
            )
            feedbackCoordinator.onSessionUpdated(
                previousSession = priorSession,
                currentSession = update.session,
                context = context,
                sampleLocation = sampleLocation,
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
        feedbackCoordinator.refreshMonitoringNotification(
            session = result.session,
            context = context,
        )
        feedbackCoordinator.onSessionUpdated(
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
                    val distanceMeters = feedbackCoordinator.resolveAlertDistanceMeters(
                        session = result.session,
                        context = context,
                    )
                    alertOrchestrator.fireAlert(
                        tripId = context.tripId,
                        intensity = context.alertIntensity,
                        mode = context.alertMode,
                        confidence = result.session.confidence.name,
                        stage = result.session.alertStage.name,
                        distanceMeters = distanceMeters,
                        etaMinutes = result.session.lastEtaMinutes,
                    )
                }

                TripSideEffect.CleanupMonitoring -> {
                    stopSelf()
                }

                TripSideEffect.RestorePersistedMonitoring -> startTracking(result.session.monitoringMode)

                TripSideEffect.LogTripCompletion -> {
                    markTripCompleted(
                        tripId = context.tripId,
                        completedAt = Clock.System.now(),
                    )
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

    private fun launchCleanup(tripId: String?) {
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            runCatching { tripCleanupUseCase() }
                .onFailure { error ->
                    analytics.recordFailure(
                        surface = "monitoring_cleanup",
                        throwable = error,
                        attributes = mapOf("trip_id" to tripId),
                    )
                    diagnosticsLogger.log(
                        eventType = "monitoring_cleanup_failed",
                        tripId = tripId,
                        payload = buildJsonObject {
                            put("message", error.message.orEmpty())
                        },
                    )
                }
        }
    }

    private fun readBatteryPercent(): Int {
        val bm = getSystemService(BATTERY_SERVICE) as? BatteryManager
        return bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)?.takeIf { it in 0..100 } ?: 100
    }

    private fun isOnCellularOnly(): Boolean {
        val cm = getSystemService(CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) &&
            !caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }

    private fun isScreenOn(): Boolean {
        val pm = getSystemService(POWER_SERVICE) as? PowerManager ?: return true
        return pm.isInteractive
    }

    private suspend fun persistSession(session: TripSession) {
        tripSessionStore.save(session)
        activeSession = session
    }

    private fun TripSession.withInitialEta(initialEtaMinutes: Int?): TripSession =
        if (lastEtaMinutes == null && initialEtaMinutes != null) {
            copy(lastEtaMinutes = initialEtaMinutes)
        } else {
            this
        }

    companion object {
        private const val NOTIFICATION_ID_MONITORING = 41
        private const val NOTIFICATION_ID_APPROACH = 43
        private const val NOTIFICATION_ID_IMMINENT = 46
        private const val NOTIFICATION_ID_TRANSFER = 44
        private const val NOTIFICATION_ID_BOARDING_WARNING = 45
        private const val BATTERY_SAVER_THRESHOLD = 20
        private const val NETWORK_SUPPRESS_THRESHOLD = 30
        private const val BALANCED_SCREEN_ON_MIN_DISTANCE = 100f
        private const val BALANCED_SCREEN_OFF_MIN_DISTANCE = 200f
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
