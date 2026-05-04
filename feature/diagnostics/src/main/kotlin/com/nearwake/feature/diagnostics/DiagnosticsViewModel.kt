package com.nearwake.feature.diagnostics

import android.Manifest
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.nearwake.core.database.dao.DiagnosticsEventDao
import com.nearwake.core.database.dao.TripSessionDao
import com.nearwake.core.database.entity.DiagnosticsEventEntity
import com.nearwake.core.datastore.UserPreferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.double
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.int
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

data class DiagnosticsEventUiModel(
    val label: String,
    val summary: String,
    val tripPrefix: String?,
    val recordedAtLabel: String,
)

data class DiagnosticsBuildInfoUiModel(
    val appVersionLabel: String,
    val buildTypeLabel: String,
    val gitShaLabel: String,
    val deviceLabel: String,
)

data class DiagnosticsPermissionStatusUiModel(
    val title: String,
    val granted: Boolean,
    val relevant: Boolean = true,
)

data class DiagnosticsPermissionSummaryUiModel(
    val readinessLabel: String,
    val summary: String,
    val statuses: List<DiagnosticsPermissionStatusUiModel>,
)

data class DiagnosticsEnvironmentUiModel(
    val powerSaverLabel: String,
    val batteryOptimizationLabel: String,
    val networkLabel: String,
)

data class DiagnosticsUiState(
    val buildInfo: DiagnosticsBuildInfoUiModel = DiagnosticsBuildInfoUiModel(
        appVersionLabel = "",
        buildTypeLabel = "",
        gitShaLabel = "",
        deviceLabel = "",
    ),
    val permissions: DiagnosticsPermissionSummaryUiModel = DiagnosticsPermissionSummaryUiModel(
        readinessLabel = "",
        summary = "",
        statuses = emptyList(),
    ),
    val environment: DiagnosticsEnvironmentUiModel = DiagnosticsEnvironmentUiModel(
        powerSaverLabel = "",
        batteryOptimizationLabel = "",
        networkLabel = "",
    ),
    val stateLabel: String = "No active trip",
    val registeredGeofences: List<String> = emptyList(),
    val recentEvents: List<DiagnosticsEventUiModel> = emptyList(),
    val diagnosticsLoggingEnabled: Boolean = false,
    val exportText: String? = null,
    val errorMessage: String? = null,
)

@HiltViewModel
class DiagnosticsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    userPreferencesDataStore: UserPreferencesDataStore,
    tripSessionDao: TripSessionDao,
    private val diagnosticsEventDao: DiagnosticsEventDao,
) : ViewModel() {
    private val mutableState = MutableStateFlow(DiagnosticsUiState())
    val state: StateFlow<DiagnosticsUiState> = mutableState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                userPreferencesDataStore.preferences,
                tripSessionDao.observeTripSessions(),
                diagnosticsEventDao.observeRecentEvents(limit = 20),
            ) { preferences, sessions, recentEvents ->
                val session = sessions.firstOrNull()
                DiagnosticsUiState(
                    buildInfo = context.toBuildInfoUiModel(),
                    permissions = context.toPermissionSummaryUiModel(),
                    environment = context.toEnvironmentUiModel(),
                    stateLabel = session?.state?.name ?: "No active trip",
                    registeredGeofences = session?.geofenceIds.orEmpty(),
                    recentEvents = recentEvents.map { it.toUiModel() },
                    diagnosticsLoggingEnabled = preferences.diagnosticsEnabled,
                )
            }.collect { uiState ->
                mutableState.value = uiState.copy(exportText = mutableState.value.exportText)
            }
        }
    }

    fun refreshSnapshot() {
        mutableState.update { state ->
            state.copy(
                buildInfo = context.toBuildInfoUiModel(),
                permissions = context.toPermissionSummaryUiModel(),
                environment = context.toEnvironmentUiModel(),
            )
        }
    }

    fun exportDiagnostics() {
        mutableState.value = mutableState.value.copy(
            exportText = buildDiagnosticsExport(mutableState.value),
        )
    }

    fun clearDiagnostics() {
        viewModelScope.launch {
            diagnosticsEventDao.clearAll()
        }
    }

    fun clearExport() {
        mutableState.value = mutableState.value.copy(exportText = null)
    }
}

private fun DiagnosticsEventEntity.toUiModel(): DiagnosticsEventUiModel {
    val payload = runCatching {
        Json.parseToJsonElement(payloadJson).jsonObject
    }.getOrNull()

    val label = eventType.replace('_', ' ')
        .split(' ')
        .joinToString(" ") { it.replaceFirstChar(Char::uppercase) }

    val summary = when (eventType) {
        "alert_fired" -> payload?.alertFiredSummary() ?: "Alert fired"
        "alert_dismissed" -> "User dismissed alert"
        "trip_state_transition" -> payload?.stateSummary() ?: "State changed"
        "trip_location_sampled" -> payload?.locationSampledSummary() ?: "Location sampled"
        "transfer_alert_fired" -> payload?.transferSummary() ?: "Transfer alert"
        "boarding_direction_warning" -> payload?.boardingSummary() ?: "Wrong direction detected"
        "boarding_direction_confirmed" -> "Boarding direction confirmed"
        "monitoring_service_started" -> payload?.startedSummary() ?: "Monitoring started"
        "geofence_registration_failed" -> "Geofence registration failed"
        "geofence_not_available" -> "Geofence unavailable — switched to GPS"
        "network_restored" -> "Signal restored — confidence recovered"
        "network_lost" -> "Signal lost — entering offline mode"
        else -> payload?.genericSummary() ?: eventType
    }

    return DiagnosticsEventUiModel(
        label = label,
        summary = summary,
        tripPrefix = tripId?.take(8),
        recordedAtLabel = recordedAt.toReadableLabel(),
    )
}

private fun kotlinx.datetime.Instant.toReadableLabel(): String =
    toString().replace('T', ' ').take(19)

private fun Context.toBuildInfoUiModel(): DiagnosticsBuildInfoUiModel {
    val packageInfo = packageManager.getPackageInfo(packageName, 0)
    val versionName = packageInfo.versionName ?: "unknown"
    val versionCode = packageInfo.longVersionCode
    return DiagnosticsBuildInfoUiModel(
        appVersionLabel = "$versionName ($versionCode)",
        buildTypeLabel = BuildConfig.BUILD_TYPE.replaceFirstChar(Char::uppercase),
        gitShaLabel = BuildConfig.BUILD_GIT_SHA,
        deviceLabel = "${Build.MANUFACTURER} ${Build.MODEL} · Android ${Build.VERSION.RELEASE}",
    )
}

private fun Context.toPermissionSummaryUiModel(): DiagnosticsPermissionSummaryUiModel {
    val notificationGranted = NotificationManagerCompat.from(this).areNotificationsEnabled()
    val fineLocationGranted = isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)
    val backgroundLocationRelevant = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
    val activityRecognitionRelevant = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
    val backgroundLocationGranted = if (backgroundLocationRelevant) {
        isPermissionGranted(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    } else {
        true
    }
    val activityRecognitionGranted = if (activityRecognitionRelevant) {
        isPermissionGranted(Manifest.permission.ACTIVITY_RECOGNITION)
    } else {
        true
    }

    val readinessLabel = when {
        !notificationGranted || !fineLocationGranted -> "Action needed"
        (backgroundLocationRelevant && !backgroundLocationGranted) ||
            (activityRecognitionRelevant && !activityRecognitionGranted) -> "Limited"
        else -> "Ready"
    }

    val summary = when (readinessLabel) {
        "Ready" -> "Notifications, precise location, and the current background permissions are in place."
        "Limited" -> "Trips can run, but screen-off reliability or power-aware behavior is reduced."
        else -> "Notifications and precise location still need attention before the app is fully ready."
    }

    return DiagnosticsPermissionSummaryUiModel(
        readinessLabel = readinessLabel,
        summary = summary,
        statuses = listOf(
            DiagnosticsPermissionStatusUiModel("Notifications", notificationGranted),
            DiagnosticsPermissionStatusUiModel("Precise location", fineLocationGranted),
            DiagnosticsPermissionStatusUiModel(
                title = "Background location",
                granted = backgroundLocationGranted,
                relevant = backgroundLocationRelevant,
            ),
            DiagnosticsPermissionStatusUiModel(
                title = "Activity recognition",
                granted = activityRecognitionGranted,
                relevant = activityRecognitionRelevant,
            ),
        ),
    )
}

private fun Context.toEnvironmentUiModel(): DiagnosticsEnvironmentUiModel {
    val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
    val networkLabel = when {
        capabilities == null -> "Offline"
        capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) -> "Connected"
        else -> "Limited"
    }
    return DiagnosticsEnvironmentUiModel(
        powerSaverLabel = if (powerManager.isPowerSaveMode) "On" else "Off",
        batteryOptimizationLabel = if (powerManager.isIgnoringBatteryOptimizations(packageName)) {
            "Ignoring optimizations"
        } else {
            "Standard battery optimization"
        },
        networkLabel = networkLabel,
    )
}

private fun Context.isPermissionGranted(permission: String): Boolean =
    ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

private fun JsonObject.alertFiredSummary(): String = buildString {
    val type = stringOrNull("type") ?: "ARRIVAL"
    val intensity = stringOrNull("intensity") ?: "STANDARD"
    val mode = stringOrNull("mode") ?: "ACTIVE"
    val confidence = stringOrNull("confidence")
    val stage = stringOrNull("stage")
    val distance = doubleOrNull2("distance_meters")
    val eta = intOrNull2("eta_minutes")

    append("$type alert — $intensity intensity, $mode mode")
    if (stage != null) append(" · stage $stage")
    if (confidence != null) append(" · confidence $confidence")
    if (distance != null) append(" · ${distance.toInt()}m away")
    if (eta != null) append(" · ~${eta}min ETA")
}

private fun JsonObject.stateSummary(): String {
    val from = stringOrNull("from_state") ?: "?"
    val to = stringOrNull("to_state") ?: "?"
    val stage = stringOrNull("stage")
    val distance = doubleOrNull2("distance_meters")
    val eta = intOrNull2("eta_minutes")
    return buildString {
        append("$from → $to")
        if (stage != null) append(" · stage $stage")
        if (distance != null) append(" · ${distance.toInt()}m")
        if (eta != null) append(" · ~${eta}min")
    }
}

private fun JsonObject.locationSampledSummary(): String {
    val distance = doubleOrNull2("distance_meters")
    val eta = intOrNull2("eta_minutes")
    val stage = stringOrNull("stage")
    return buildString {
        if (distance != null) append("${distance.toInt()}m away")
        if (eta != null) {
            if (isNotEmpty()) append(" · ")
            append("~${eta}min ETA")
        }
        if (stage != null) {
            if (isNotEmpty()) append(" · ")
            append("stage $stage")
        }
    }.ifBlank { "Position sampled" }
}

private fun JsonObject.transferSummary(): String {
    val stop = stringOrNull("stop_name") ?: "stop"
    val line = stringOrNull("line_name") ?: ""
    val minutes = intOrNull2("remaining_minutes") ?: 0
    return if (minutes <= 1) "Change to $line at $stop — now" else "Change to $line at $stop in ${minutes}min"
}

private fun JsonObject.boardingSummary(): String {
    val expected = doubleOrNull2("expected_heading_degrees")
    val actual = doubleOrNull2("actual_heading_degrees")
    return if (expected != null && actual != null) {
        "Expected ${expected.toInt()}°, moving ${actual.toInt()}° — possible wrong direction"
    } else {
        "Heading mismatch detected"
    }
}

private fun JsonObject.startedSummary(): String {
    val state = stringOrNull("state") ?: "?"
    val mode = stringOrNull("mode") ?: "?"
    val route = get("route_cached")?.jsonPrimitive?.content
    return "Started — state $state, $mode mode, route cached: $route"
}

private fun JsonObject.genericSummary(): String =
    this.entries.take(3).joinToString(" · ") { (k, v) -> "$k: ${v.jsonPrimitive.content}" }

private fun JsonObject.stringOrNull(key: String): String? =
    runCatching { get(key)?.jsonPrimitive?.content }.getOrNull()

private fun JsonObject.doubleOrNull2(key: String): Double? =
    runCatching { get(key)?.jsonPrimitive?.double }.getOrNull()

private fun JsonObject.intOrNull2(key: String): Int? =
    runCatching { get(key)?.jsonPrimitive?.int }.getOrNull()
