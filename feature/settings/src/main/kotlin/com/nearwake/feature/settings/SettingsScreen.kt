package com.nearwake.feature.settings

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nearwake.core.datastore.model.ThemeMode
import com.nearwake.core.designsystem.LocalNearWakeColors
import com.nearwake.core.designsystem.LocalSpacing
import com.nearwake.core.designsystem.ProvideNearWakeStateAccent
import com.nearwake.core.ui.ElevatedCard
import com.nearwake.core.ui.NearWakeChipState
import com.nearwake.core.ui.NearWakeScaffold
import com.nearwake.core.ui.NearWakeSectionHeader
import com.nearwake.core.ui.NearWakeSelectableChip
import com.nearwake.core.ui.NearWakeStateChip
import com.nearwake.core.ui.NearWakeTextButton
import com.nearwake.core.ui.SurfaceCard
import com.nearwake.domain.trip.model.AlertIntensity
import com.nearwake.domain.trip.model.AlertMode
import com.nearwake.domain.trip.model.AlertTriggerMode

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onDiagnostics: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val spacing = LocalSpacing.current
    val themeColors = LocalNearWakeColors.current
    val guidance = oemReliabilityGuidance(
        manufacturer = Build.MANUFACTURER,
        backgroundMonitoringEnabled = state.backgroundMonitoringEnabled,
    )

    ProvideNearWakeStateAccent(themeColors.monitoringBase) {
        NearWakeScaffold(
            title = "Settings",
            subtitle = null,
            topBarActions = {
                NearWakeTextButton(text = "Diagnostics", onClick = onDiagnostics)
                NearWakeTextButton(text = "Back", onClick = onBack)
            },
        ) {
            // Appearance section
            SurfaceCard {
                NearWakeSectionHeader(text = "Appearance")
                Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
                    Text(
                        text = "Theme",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                        verticalArrangement = Arrangement.spacedBy(spacing.sm),
                    ) {
                        ThemeMode.entries.forEach { themeMode ->
                            NearWakeSelectableChip(
                                selected = state.themeMode == themeMode,
                                onClick = { viewModel.updateThemeMode(themeMode) },
                                label = themeMode.label,
                            )
                        }
                    }
                }
            }

            // Alerts section
            SurfaceCard {
                NearWakeSectionHeader(text = "Alerts")
                Column(verticalArrangement = Arrangement.spacedBy(spacing.md)) {
                    Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
                        Text(
                            text = "Default trigger",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                            verticalArrangement = Arrangement.spacedBy(spacing.sm),
                        ) {
                            AlertTriggerMode.entries.forEach { triggerMode ->
                                NearWakeSelectableChip(
                                    selected = state.defaultAlertTriggerMode == triggerMode,
                                    onClick = { viewModel.updateAlertTriggerMode(triggerMode) },
                                    label = triggerMode.label,
                                )
                            }
                        }
                    }
                    if (state.defaultAlertTriggerMode.usesTimeTrigger()) {
                        Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
                        Text(
                            text = "Default lead time",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                            verticalArrangement = Arrangement.spacedBy(spacing.sm),
                        ) {
                            NearWakeSelectableChip(
                                selected = state.defaultAlertLeadMinutes == 0,
                                onClick = { viewModel.updateLeadMinutes(0) },
                                label = "Nearby",
                            )
                            listOf(2, 5, 10, 15).forEach { minutes ->
                                NearWakeSelectableChip(
                                    selected = state.defaultAlertLeadMinutes == minutes,
                                    onClick = { viewModel.updateLeadMinutes(minutes) },
                                    label = "$minutes min",
                                )
                            }
                        }
                    }
                    }
                    if (state.defaultAlertTriggerMode.usesDistanceTrigger()) {
                        Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
                            Text(
                                text = "Default distance",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                                verticalArrangement = Arrangement.spacedBy(spacing.sm),
                            ) {
                                listOf(500, 1000, 1500, 2000).forEach { distanceMeters ->
                                    NearWakeSelectableChip(
                                        selected = state.defaultAlertDistanceMeters == distanceMeters,
                                        onClick = { viewModel.updateAlertDistanceMeters(distanceMeters) },
                                        label = distanceMeters.toDistanceLabel(),
                                    )
                                }
                            }
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
                        Text(
                            text = "Default intensity",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                            verticalArrangement = Arrangement.spacedBy(spacing.sm),
                        ) {
                            AlertIntensity.entries.forEach { intensity ->
                                NearWakeSelectableChip(
                                    selected = state.alertIntensity == intensity,
                                    onClick = { viewModel.updateAlertIntensity(intensity) },
                                    label = intensity.name.lowercase().replaceFirstChar(Char::uppercase),
                                )
                            }
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
                        Text(
                            text = "Default mode",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                            verticalArrangement = Arrangement.spacedBy(spacing.sm),
                        ) {
                            AlertMode.entries.forEach { mode ->
                                NearWakeSelectableChip(
                                    selected = state.alertMode == mode,
                                    onClick = { viewModel.updateAlertMode(mode) },
                                    label = mode.label,
                                )
                            }
                        }
                    }
                }
            }

            // Monitoring section
            SurfaceCard {
                NearWakeSectionHeader(text = "Monitoring")
                Column(verticalArrangement = Arrangement.spacedBy(spacing.md)) {
                    SettingSwitchRow(
                        title = "Background monitoring",
                        subtitle = "Keeps trip monitoring alive while the phone is locked.",
                        checked = state.backgroundMonitoringEnabled,
                        onCheckedChange = viewModel::setBackgroundMonitoringEnabled,
                    )
                    SettingSwitchRow(
                        title = "Diagnostics logging",
                        subtitle = "Stores recent engine events on this device for troubleshooting. Clear them from Diagnostics.",
                        checked = state.diagnosticsEnabled,
                        onCheckedChange = viewModel::setDiagnosticsEnabled,
                    )
                }
            }

            // OEM Reliability section
            ElevatedCard {
                NearWakeSectionHeader(text = "Reliability")
                NearWakeStateChip(
                    label = guidance.statusLabel,
                    state = when (guidance.statusTone) {
                        OemReliabilityTone.Stable -> NearWakeChipState.Safe
                        OemReliabilityTone.Review -> NearWakeChipState.Approaching
                        OemReliabilityTone.Degraded -> NearWakeChipState.Alert
                    },
                )
                Text(
                    text = "${guidance.manufacturerLabel} device",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = guidance.summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (guidance.steps.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
                        guidance.steps.forEachIndexed { index, step ->
                            Text(
                                text = "${index + 1}. $step",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                        }
                    }
                }
                Text(
                    text = "NearWake cannot verify OEM battery exemptions automatically. Check your device's battery settings if alerts seem late.",
                    style = MaterialTheme.typography.bodySmall,
                    color = themeColors.textTertiary,
                )
            }
        }
    }
}

private val AlertMode.label: String
    get() = when (this) {
        AlertMode.ACTIVE -> "Active"
        AlertMode.SLEEP -> "Sleep"
    }

private val AlertTriggerMode.label: String
    get() = when (this) {
        AlertTriggerMode.TIME -> "Time"
        AlertTriggerMode.DISTANCE -> "Distance"
        AlertTriggerMode.BOTH -> "Both"
    }

private val ThemeMode.label: String
    get() = when (this) {
        ThemeMode.SYSTEM -> "System"
        ThemeMode.LIGHT -> "Light"
        ThemeMode.DARK -> "Dark"
    }

private fun AlertTriggerMode.usesTimeTrigger(): Boolean = this != AlertTriggerMode.DISTANCE

private fun AlertTriggerMode.usesDistanceTrigger(): Boolean = this != AlertTriggerMode.TIME

private fun Int.toDistanceLabel(): String =
    if (this >= 1000) {
        val kilometers = this / 1000.0
        if (kilometers % 1.0 == 0.0) "${kilometers.toInt()} km" else "${kilometers} km"
    } else {
        "$this m"
    }

@Composable
private fun SettingSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val spacing = LocalSpacing.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(
            modifier = Modifier.weight(1f).padding(end = spacing.md),
            verticalArrangement = Arrangement.spacedBy(spacing.xs),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}
