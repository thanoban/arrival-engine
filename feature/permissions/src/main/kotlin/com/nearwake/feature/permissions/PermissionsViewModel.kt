package com.nearwake.feature.permissions

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class PermissionReadiness {
    READY,
    LIMITED,
    ACTION_NEEDED,
}

enum class PermissionPromptAction {
    REFRESH,
    REQUEST_CORE,
    REQUEST_BACKGROUND,
    OPEN_SETTINGS,
}

data class PermissionStatusUiModel(
    val title: String,
    val body: String,
    val granted: Boolean,
    val relevant: Boolean = true,
)

data class PermissionSnapshot(
    val notificationGranted: Boolean = false,
    val fineLocationGranted: Boolean = false,
    val backgroundLocationGranted: Boolean = false,
    val activityRecognitionGranted: Boolean = false,
    val notificationsRequestable: Boolean = false,
    val backgroundLocationRelevant: Boolean = false,
    val activityRecognitionRelevant: Boolean = false,
) {
    val notificationsMissing: Boolean
        get() = !notificationGranted

    val fineLocationMissing: Boolean
        get() = !fineLocationGranted

    val backgroundLocationMissing: Boolean
        get() = backgroundLocationRelevant && !backgroundLocationGranted

    val activityRecognitionMissing: Boolean
        get() = activityRecognitionRelevant && !activityRecognitionGranted
}

data class PermissionsUiState(
    val notification: PermissionStatusUiModel = PermissionStatusUiModel(
        title = "Notifications",
        body = "NearWake can't wake you near your stop unless Android is allowed to show alarms.",
        granted = false,
    ),
    val fineLocation: PermissionStatusUiModel = PermissionStatusUiModel(
        title = "Precise location",
        body = "Location lets NearWake know where your trip is starting and where your destination is.",
        granted = false,
    ),
    val backgroundLocation: PermissionStatusUiModel = PermissionStatusUiModel(
        title = "Background location",
        body = "To alert you while your screen is off or you're asleep, NearWake needs background location during active trips only.",
        granted = false,
        relevant = false,
    ),
    val activityRecognition: PermissionStatusUiModel = PermissionStatusUiModel(
        title = "Activity recognition",
        body = "Helps the engine scale power use up and down more intelligently based on whether you're moving.",
        granted = false,
        relevant = false,
    ),
    val readiness: PermissionReadiness = PermissionReadiness.ACTION_NEEDED,
    val readinessLabel: String = "Action needed",
    val summary: String = "NearWake cannot reliably alert you until core permissions are enabled.",
    val primaryAction: PermissionPromptAction = PermissionPromptAction.REQUEST_CORE,
    val primaryButtonLabel: String = "Request core permissions",
    val errorMessage: String? = null,
)

class PermissionsViewModel : ViewModel() {
    private val mutableState = MutableStateFlow(PermissionsUiState())
    val state: StateFlow<PermissionsUiState> = mutableState.asStateFlow()

    fun updateSnapshot(snapshot: PermissionSnapshot) {
        mutableState.value = snapshot.toUiState()
    }
}

internal fun PermissionSnapshot.toUiState(): PermissionsUiState {
    val readiness = when {
        notificationsMissing || fineLocationMissing -> PermissionReadiness.ACTION_NEEDED
        backgroundLocationMissing || activityRecognitionMissing -> PermissionReadiness.LIMITED
        else -> PermissionReadiness.READY
    }
    val primaryAction = when {
        fineLocationMissing || activityRecognitionMissing -> PermissionPromptAction.REQUEST_CORE
        notificationsMissing && notificationsRequestable -> PermissionPromptAction.REQUEST_CORE
        backgroundLocationMissing -> PermissionPromptAction.REQUEST_BACKGROUND
        notificationsMissing -> PermissionPromptAction.OPEN_SETTINGS
        else -> PermissionPromptAction.REFRESH
    }
    return PermissionsUiState(
        notification = PermissionStatusUiModel(
            title = "Notifications",
            body = "NearWake can't wake you near your stop unless Android is allowed to show alarms.",
            granted = notificationGranted,
        ),
        fineLocation = PermissionStatusUiModel(
            title = "Precise location",
            body = "Location lets NearWake know where your trip is starting and where your destination is.",
            granted = fineLocationGranted,
        ),
        backgroundLocation = PermissionStatusUiModel(
            title = "Background location",
            body = "To alert you while your screen is off or you're asleep, NearWake needs background location during active trips only.",
            granted = backgroundLocationGranted,
            relevant = backgroundLocationRelevant,
        ),
        activityRecognition = PermissionStatusUiModel(
            title = "Activity recognition",
            body = "Helps the engine scale power use up and down more intelligently based on whether you're moving.",
            granted = activityRecognitionGranted,
            relevant = activityRecognitionRelevant,
        ),
        readiness = readiness,
        readinessLabel = when (readiness) {
            PermissionReadiness.READY -> "Ready"
            PermissionReadiness.LIMITED -> "Limited"
            PermissionReadiness.ACTION_NEEDED -> "Action needed"
        },
        summary = when (readiness) {
            PermissionReadiness.READY ->
                "Arrival alerts, departure reminders, and background monitoring are ready."
            PermissionReadiness.LIMITED ->
                "Trips can run, but screen-off reliability or power balancing is reduced until the remaining permissions are enabled."
            PermissionReadiness.ACTION_NEEDED ->
                "NearWake cannot reliably alert you until notifications and precise location are enabled."
        },
        primaryAction = primaryAction,
        primaryButtonLabel = when (primaryAction) {
            PermissionPromptAction.REFRESH -> "Refresh status"
            PermissionPromptAction.REQUEST_CORE -> "Request core permissions"
            PermissionPromptAction.REQUEST_BACKGROUND -> "Enable background location"
            PermissionPromptAction.OPEN_SETTINGS -> "Open app settings"
        },
    )
}
