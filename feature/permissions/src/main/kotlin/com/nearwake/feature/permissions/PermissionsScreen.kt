package com.nearwake.feature.permissions

import android.Manifest
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nearwake.core.designsystem.LocalSpacing
import com.nearwake.core.designsystem.NearWakeColors
import com.nearwake.core.designsystem.ProvideNearWakeStateAccent
import com.nearwake.core.ui.NearWakePrimaryButton
import com.nearwake.core.ui.NearWakeScaffold
import com.nearwake.core.ui.NearWakeSecondaryButton
import com.nearwake.core.ui.NearWakeStateChip
import com.nearwake.core.ui.NearWakeTextButton
import com.nearwake.core.ui.SurfaceCard

@Composable
fun PermissionsScreen(
    onUseLimitedMode: () -> Unit,
    onBack: () -> Unit,
    viewModel: PermissionsViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val spacing = LocalSpacing.current
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }

    val refreshPermissions = remember(context, viewModel) {
        { viewModel.updateSnapshot(context.permissionSnapshot()) }
    }

    val corePermissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) {
        refreshPermissions()
    }
    val backgroundLocationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) {
        refreshPermissions()
    }

    DisposableEffect(context) {
        refreshPermissions()
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                refreshPermissions()
            }
        }
        val lifecycle = activity?.lifecycle
        lifecycle?.addObserver(observer)
        onDispose {
            lifecycle?.removeObserver(observer)
        }
    }

    ProvideNearWakeStateAccent(NearWakeColors.SafeBase) {
        NearWakeScaffold(
            title = "Permissions",
            subtitle = null,
            topBarActions = {
                NearWakeTextButton(text = "Back", onClick = onBack)
            },
        ) {
            SurfaceCard {
                NearWakeStateChip(
                    label = state.readinessLabel,
                    state = when (state.readiness) {
                        PermissionReadiness.READY -> com.nearwake.core.ui.NearWakeChipState.Safe
                        PermissionReadiness.LIMITED -> com.nearwake.core.ui.NearWakeChipState.Approaching
                        PermissionReadiness.ACTION_NEEDED -> com.nearwake.core.ui.NearWakeChipState.Alert
                    },
                )
                Text(
                    text = state.summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "Android may require app settings for denied notifications or background location on some devices.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(spacing.md)) {
                PermissionStatusCard(state.notification)
                PermissionStatusCard(state.fineLocation)
                PermissionStatusCard(state.backgroundLocation)
                PermissionStatusCard(state.activityRecognition)
            }

            NearWakePrimaryButton(
                modifier = Modifier.fillMaxWidth(),
                text = state.primaryButtonLabel,
                onClick = {
                    when (state.primaryAction) {
                        PermissionPromptAction.REFRESH -> refreshPermissions()
                        PermissionPromptAction.REQUEST_CORE -> {
                            corePermissionsLauncher.launch(context.corePermissions())
                        }
                        PermissionPromptAction.REQUEST_BACKGROUND -> {
                            backgroundLocationLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                        }
                        PermissionPromptAction.OPEN_SETTINGS -> context.openAppSettings()
                    }
                },
            )
            NearWakeSecondaryButton(
                modifier = Modifier.fillMaxWidth(),
                text = "Open app settings",
                onClick = { context.openAppSettings() },
            )
            NearWakeTextButton(
                modifier = Modifier.fillMaxWidth(),
                text = "Use limited mode",
                onClick = onUseLimitedMode,
            )
        }
    }
}

@Composable
private fun PermissionStatusCard(
    permission: PermissionStatusUiModel,
) {
    if (!permission.relevant) return

    SurfaceCard {
        NearWakeStateChip(
            label = if (permission.granted) "Granted" else "Needed",
            state = if (permission.granted) {
                com.nearwake.core.ui.NearWakeChipState.Safe
            } else {
                com.nearwake.core.ui.NearWakeChipState.Approaching
            },
        )
        Text(
            text = permission.title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = permission.body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun Context.permissionSnapshot(): PermissionSnapshot =
    PermissionSnapshot(
        notificationGranted = NotificationManagerCompat.from(this).areNotificationsEnabled(),
        fineLocationGranted = isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION),
        backgroundLocationGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            isPermissionGranted(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        } else {
            true
        },
        activityRecognitionGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            isPermissionGranted(Manifest.permission.ACTIVITY_RECOGNITION)
        } else {
            true
        },
        notificationsRequestable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU,
        backgroundLocationRelevant = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q,
        activityRecognitionRelevant = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q,
    )

private fun Context.corePermissions(): Array<String> =
    buildList {
        add(Manifest.permission.ACCESS_FINE_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            add(Manifest.permission.ACTIVITY_RECOGNITION)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }.toTypedArray()

private fun Context.isPermissionGranted(permission: String): Boolean =
    ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

private fun Context.openAppSettings() {
    startActivity(
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        },
    )
}

private fun Context.findActivity(): ComponentActivity? =
    when (this) {
        is ComponentActivity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
