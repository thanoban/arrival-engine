package com.nearwake.feature.diagnostics

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nearwake.core.designsystem.LocalSpacing
import com.nearwake.core.designsystem.NearWakeColors
import com.nearwake.core.designsystem.ProvideNearWakeStateAccent
import com.nearwake.core.ui.NearWakeChipState
import com.nearwake.core.ui.NearWakeScaffold
import com.nearwake.core.ui.NearWakeSectionHeader
import com.nearwake.core.ui.NearWakeStateChip
import com.nearwake.core.ui.NearWakeTextButton
import com.nearwake.core.ui.SurfaceCard

@Composable
fun DiagnosticsScreen(
    onBack: () -> Unit,
    viewModel: DiagnosticsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val spacing = LocalSpacing.current
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(state.exportText) {
        val exportText = state.exportText ?: return@LaunchedEffect
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "NearWake diagnostics export")
            putExtra(Intent.EXTRA_TEXT, exportText)
        }
        context.startActivity(Intent.createChooser(intent, "Export diagnostics"))
        viewModel.clearExport()
    }

    DisposableEffect(lifecycleOwner, viewModel) {
        viewModel.refreshSnapshot()
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshSnapshot()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    ProvideNearWakeStateAccent(NearWakeColors.MonitoringBase) {
        NearWakeScaffold(
            title = "Diagnostics",
            subtitle = null,
            topBarActions = {
                NearWakeTextButton(text = "Refresh", onClick = viewModel::refreshSnapshot)
                if (
                    state.buildInfo.appVersionLabel.isNotBlank() ||
                    state.registeredGeofences.isNotEmpty() ||
                    state.recentEvents.isNotEmpty()
                ) {
                    NearWakeTextButton(text = "Export", onClick = viewModel::exportDiagnostics)
                }
                NearWakeTextButton(text = "Back", onClick = onBack)
            },
        ) {
            SurfaceCard {
                NearWakeSectionHeader(text = "Build and device")
                Text(
                    text = "App version",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = state.buildInfo.appVersionLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "Build type",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = state.buildInfo.buildTypeLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "Device",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = state.buildInfo.deviceLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            SurfaceCard {
                NearWakeSectionHeader(text = "Permissions")
                NearWakeStateChip(
                    label = state.permissions.readinessLabel,
                    state = when (state.permissions.readinessLabel) {
                        "Ready" -> NearWakeChipState.Safe
                        "Limited" -> NearWakeChipState.Approaching
                        else -> NearWakeChipState.Alert
                    },
                )
                Text(
                    text = state.permissions.summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Column(verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
                    state.permissions.statuses.filter { it.relevant }.forEach { permission ->
                        Text(
                            text = "${permission.title}: ${if (permission.granted) "Granted" else "Needed"}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                }
            }

            SurfaceCard {
                NearWakeSectionHeader(text = "Environment")
                Text(
                    text = "Power saver: ${state.environment.powerSaverLabel}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = "Battery optimization: ${state.environment.batteryOptimizationLabel}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = "Network: ${state.environment.networkLabel}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }

            SurfaceCard {
                NearWakeSectionHeader(text = "Services")
                NearWakeStateChip(
                    label = state.stateLabel.replace('_', ' ').lowercase().replaceFirstChar(Char::uppercase),
                    state = if (state.stateLabel == "No active trip") NearWakeChipState.Neutral else NearWakeChipState.Monitoring,
                )
            }

            SurfaceCard {
                NearWakeSectionHeader(text = "Location")
                Text(
                    text = "Registered geofences",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Column(verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
                    if (state.registeredGeofences.isEmpty()) {
                        Text(
                            text = "No geofences registered",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        state.registeredGeofences.forEach { geofence ->
                            Text(
                                text = geofence,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                        }
                    }
                }
            }

            SurfaceCard {
                NearWakeSectionHeader(text = "Last session — why each event fired")
                if (state.recentEvents.isEmpty()) {
                    Text(
                        text = "No diagnostics events recorded yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(spacing.md)) {
                        state.recentEvents.forEach { event ->
                            DiagnosticsEventRow(event = event)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DiagnosticsEventRow(event: DiagnosticsEventUiModel) {
    val spacing = LocalSpacing.current
    Column(verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
        Text(
            text = event.tripPrefix?.let { "${event.recordedAtLabel} · ${event.label} · $it" }
                ?: "${event.recordedAtLabel} · ${event.label}",
            style = MaterialTheme.typography.labelMedium,
            color = NearWakeColors.TextSecondary,
        )
        Text(
            text = event.summary,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}
