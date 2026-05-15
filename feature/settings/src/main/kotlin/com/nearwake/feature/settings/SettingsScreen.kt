package com.nearwake.feature.settings

import android.content.Context
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nearwake.core.datastore.model.ThemeMode
import com.nearwake.core.designsystem.LocalNearWakeColors
import com.nearwake.core.designsystem.LocalSpacing
import com.nearwake.core.designsystem.ProvideNearWakeStateAccent
import com.nearwake.core.ui.NearWakeChipState
import com.nearwake.core.ui.NearWakeScaffold
import com.nearwake.core.ui.NearWakeSectionHeader
import com.nearwake.core.ui.NearWakeStateChip
import com.nearwake.core.ui.NearWakeTextButton
import com.nearwake.domain.trip.model.AlertIntensity
import com.nearwake.domain.trip.model.AlertMode
import com.nearwake.domain.trip.model.AlertTriggerMode

private enum class SettingsSheet {
    Theme,
    TriggerMode,
    LeadTime,
    Distance,
    Intensity,
    AlertMode,
}

private data class SelectorOption(
    val label: String,
    val supportingText: String? = null,
    val selected: Boolean,
    val onSelect: () -> Unit,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onDiagnostics: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val spacing = LocalSpacing.current
    val colors = LocalNearWakeColors.current
    val context = LocalContext.current
    val guidance = oemReliabilityGuidance(
        manufacturer = Build.MANUFACTURER,
        backgroundMonitoringEnabled = state.backgroundMonitoringEnabled,
    )
    val appVersionLabel = remember(context) { context.appVersionLabel() }
    var activeSheet by remember { mutableStateOf<SettingsSheet?>(null) }
    var reliabilityExpanded by rememberSaveable { mutableStateOf(false) }

    ProvideNearWakeStateAccent(colors.monitoringBase) {
        NearWakeScaffold(
            title = "Settings",
            subtitle = null,
            topBarActions = {
                NearWakeTextButton(text = "Back", onClick = onBack)
            },
        ) {
            SettingsSection("Alerts") {
                SettingsSelectorRow(
                    icon = Icons.Filled.Tune,
                    label = "Alert trigger",
                    value = state.defaultAlertTriggerMode.label,
                    onClick = { activeSheet = SettingsSheet.TriggerMode },
                )
                SettingsDivider()
                if (state.defaultAlertTriggerMode.usesTimeTrigger()) {
                    SettingsSelectorRow(
                        icon = Icons.Filled.Schedule,
                        label = "Lead time",
                        value = state.defaultAlertLeadMinutes.toLeadTimeLabel(),
                        onClick = { activeSheet = SettingsSheet.LeadTime },
                    )
                    SettingsDivider()
                }
                if (state.defaultAlertTriggerMode.usesDistanceTrigger()) {
                    SettingsSelectorRow(
                        icon = Icons.Filled.MyLocation,
                        label = "Distance trigger",
                        value = state.defaultAlertDistanceMeters.toDistanceLabel(),
                        onClick = { activeSheet = SettingsSheet.Distance },
                    )
                    SettingsDivider()
                }
                SettingsSelectorRow(
                    icon = Icons.Filled.Alarm,
                    label = "Alert intensity",
                    value = state.alertIntensity.label,
                    onClick = { activeSheet = SettingsSheet.Intensity },
                )
            }

            SettingsSection("Monitoring") {
                SettingsSelectorRow(
                    icon = if (state.alertMode == AlertMode.ACTIVE) Icons.Filled.FlashOn else Icons.Filled.Bedtime,
                    label = "Default mode",
                    value = state.alertMode.label,
                    onClick = { activeSheet = SettingsSheet.AlertMode },
                )
                SettingsDivider()
                SettingsSwitchRow(
                    icon = Icons.Filled.Bedtime,
                    label = "Background monitoring",
                    value = "Keep trips alive with the screen off",
                    checked = state.backgroundMonitoringEnabled,
                    onCheckedChange = viewModel::setBackgroundMonitoringEnabled,
                )
            }

            SettingsSection("Appearance") {
                SettingsSelectorRow(
                    icon = Icons.Filled.ColorLens,
                    label = "Theme",
                    value = state.themeMode.label,
                    onClick = { activeSheet = SettingsSheet.Theme },
                )
            }

            SettingsSection("Reliability") {
                SettingsExpandableRow(
                    icon = Icons.Filled.Warning,
                    label = "OEM battery guide",
                    value = guidance.statusLabel,
                    expanded = reliabilityExpanded,
                    onClick = { reliabilityExpanded = !reliabilityExpanded },
                )
                AnimatedVisibility(visible = reliabilityExpanded) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 36.dp, top = spacing.xs),
                        verticalArrangement = Arrangement.spacedBy(spacing.sm),
                    ) {
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
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textPrimary,
                        )
                        Text(
                            text = guidance.summary,
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.textSecondary,
                        )
                        guidance.steps.forEachIndexed { index, step ->
                            Text(
                                text = "${index + 1}. $step",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.textSecondary,
                            )
                        }
                    }
                }
            }

            SettingsSection("Privacy") {
                SettingsSwitchRow(
                    icon = Icons.Filled.BugReport,
                    label = "Diagnostics sharing",
                    value = "Store recent engine events on this device",
                    checked = state.diagnosticsEnabled,
                    onCheckedChange = viewModel::setDiagnosticsEnabled,
                )
                SettingsDivider()
                SettingsSelectorRow(
                    icon = Icons.Filled.BugReport,
                    label = "View diagnostics",
                    value = "Inspect logs and device readiness",
                    onClick = onDiagnostics,
                )
            }

            SettingsSection("About") {
                SettingsValueRow(
                    icon = Icons.Filled.ColorLens,
                    label = "App version",
                    value = appVersionLabel,
                )
            }
        }
    }

    val selectorOptions = rememberSettingsSelectorOptions(
        state = state,
        viewModel = viewModel,
        onDismiss = { activeSheet = null },
    )

    activeSheet?.let { sheet ->
        val sheetContent = selectorOptions.getValue(sheet)
        ModalBottomSheet(
            onDismissRequest = { activeSheet = null },
            containerColor = colors.bgSurface,
        ) {
            SettingsSelectorSheet(
                title = sheetContent.first,
                options = sheetContent.second,
            )
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    val spacing = LocalSpacing.current
    Column(verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
        NearWakeSectionHeader(text = title)
        content()
    }
}

@Composable
private fun SettingsSelectorRow(
    icon: ImageVector,
    label: String,
    value: String,
    onClick: () -> Unit,
) {
    SettingsRow(
        icon = icon,
        label = label,
        value = value,
        trailing = {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = LocalNearWakeColors.current.textTertiary,
            )
        },
        onClick = onClick,
    )
}

@Composable
private fun SettingsValueRow(
    icon: ImageVector,
    label: String,
    value: String,
) {
    SettingsRow(
        icon = icon,
        label = label,
        value = null,
        trailing = {
            Text(
                text = value,
                style = MaterialTheme.typography.labelSmall,
                color = LocalNearWakeColors.current.textSecondary,
            )
        },
    )
}

@Composable
private fun SettingsSwitchRow(
    icon: ImageVector,
    label: String,
    value: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    SettingsRow(
        icon = icon,
        label = label,
        value = value,
        trailing = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
            )
        },
    )
}

@Composable
private fun SettingsExpandableRow(
    icon: ImageVector,
    label: String,
    value: String,
    expanded: Boolean,
    onClick: () -> Unit,
) {
    SettingsRow(
        icon = icon,
        label = label,
        value = value,
        trailing = {
            Icon(
                imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                contentDescription = null,
                tint = LocalNearWakeColors.current.textTertiary,
            )
        },
        onClick = onClick,
    )
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    label: String,
    value: String?,
    trailing: @Composable () -> Unit,
    onClick: (() -> Unit)? = null,
) {
    val spacing = LocalSpacing.current
    val colors = LocalNearWakeColors.current
    val clickableModifier = if (onClick != null) {
        Modifier
            .semantics {
                role = Role.Button
                contentDescription = if (value.isNullOrBlank()) {
                    label
                } else {
                    "$label. $value"
                }
            }
            .clickable(onClick = onClick)
    } else {
        Modifier
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(clickableModifier)
            .defaultMinSize(minHeight = 52.dp)
            .padding(vertical = spacing.sm),
        horizontalArrangement = Arrangement.spacedBy(spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = colors.textSecondary,
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(spacing.xs),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textPrimary,
            )
            value?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textSecondary,
                )
            }
        }
        trailing()
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        modifier = Modifier.fillMaxWidth(),
        thickness = 1.dp,
        color = LocalNearWakeColors.current.borderSubtle,
    )
}

@Composable
private fun SettingsSelectorSheet(
    title: String,
    options: List<SelectorOption>,
) {
    val spacing = LocalSpacing.current
    val colors = LocalNearWakeColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.xl, vertical = spacing.md),
        verticalArrangement = Arrangement.spacedBy(spacing.sm),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = colors.textPrimary,
        )
        options.forEachIndexed { index, option ->
            SelectorOptionRow(option = option)
            if (index < options.lastIndex) {
                SettingsDivider()
            }
        }
        Spacer(modifier = Modifier.size(spacing.xl))
    }
}

@Composable
private fun SelectorOptionRow(option: SelectorOption) {
    val spacing = LocalSpacing.current
    val colors = LocalNearWakeColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                role = Role.Button
                stateDescription = if (option.selected) "Selected" else "Not selected"
                contentDescription = buildString {
                    append(option.label)
                    option.supportingText?.takeIf { it.isNotBlank() }?.let {
                        append(". ")
                        append(it)
                    }
                }
            }
            .clickable(onClick = option.onSelect)
            .defaultMinSize(minHeight = 52.dp)
            .padding(vertical = spacing.sm),
        horizontalArrangement = Arrangement.spacedBy(spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(spacing.xs),
        ) {
            Text(
                text = option.label,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textPrimary,
            )
            option.supportingText?.let { supportingText ->
                Text(
                    text = supportingText,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textSecondary,
                )
            }
        }
        if (option.selected) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = colors.monitoringBase,
            )
        }
    }
}

@Composable
private fun rememberSettingsSelectorOptions(
    state: SettingsUiState,
    viewModel: SettingsViewModel,
    onDismiss: () -> Unit,
): Map<SettingsSheet, Pair<String, List<SelectorOption>>> {
    return remember(state, viewModel) {
        mapOf(
            SettingsSheet.Theme to (
                "Theme" to ThemeMode.entries.map { themeMode ->
                    SelectorOption(
                        label = themeMode.label,
                        selected = state.themeMode == themeMode,
                        onSelect = {
                            viewModel.updateThemeMode(themeMode)
                            onDismiss()
                        },
                    )
                }
            ),
            SettingsSheet.TriggerMode to (
                "Alert trigger" to AlertTriggerMode.entries.map { triggerMode ->
                    SelectorOption(
                        label = triggerMode.label,
                        supportingText = triggerMode.description,
                        selected = state.defaultAlertTriggerMode == triggerMode,
                        onSelect = {
                            viewModel.updateAlertTriggerMode(triggerMode)
                            onDismiss()
                        },
                    )
                }
            ),
            SettingsSheet.LeadTime to (
                "Lead time" to buildList {
                    add(
                        SelectorOption(
                            label = "Nearby",
                            selected = state.defaultAlertLeadMinutes == 0,
                            onSelect = {
                                viewModel.updateLeadMinutes(0)
                                onDismiss()
                            },
                        ),
                    )
                    listOf(2, 5, 10, 15).forEach { minutes ->
                        add(
                            SelectorOption(
                                label = "$minutes min",
                                selected = state.defaultAlertLeadMinutes == minutes,
                                onSelect = {
                                    viewModel.updateLeadMinutes(minutes)
                                    onDismiss()
                                },
                            ),
                        )
                    }
                }
            ),
            SettingsSheet.Distance to (
                "Distance trigger" to listOf(500, 1000, 1500, 2000).map { distanceMeters ->
                    SelectorOption(
                        label = distanceMeters.toDistanceLabel(),
                        selected = state.defaultAlertDistanceMeters == distanceMeters,
                        onSelect = {
                            viewModel.updateAlertDistanceMeters(distanceMeters)
                            onDismiss()
                        },
                    )
                }
            ),
            SettingsSheet.Intensity to (
                "Alert intensity" to AlertIntensity.entries.map { intensity ->
                    SelectorOption(
                        label = intensity.label,
                        selected = state.alertIntensity == intensity,
                        onSelect = {
                            viewModel.updateAlertIntensity(intensity)
                            onDismiss()
                        },
                    )
                }
            ),
            SettingsSheet.AlertMode to (
                "Default mode" to AlertMode.entries.map { mode ->
                    SelectorOption(
                        label = mode.label,
                        supportingText = mode.description,
                        selected = state.alertMode == mode,
                        onSelect = {
                            viewModel.updateAlertMode(mode)
                            onDismiss()
                        },
                    )
                }
            ),
        )
    }
}

private val AlertIntensity.label: String
    get() = name.lowercase().replaceFirstChar(Char::uppercase)

private val AlertMode.description: String
    get() = when (this) {
        AlertMode.ACTIVE -> "Keeps alerts concise while you stay awake and distracted."
        AlertMode.SLEEP -> "Prepares stronger alerts for naps and locked-screen travel."
    }

private val AlertMode.label: String
    get() = when (this) {
        AlertMode.ACTIVE -> "Active"
        AlertMode.SLEEP -> "Sleep"
    }

private val AlertTriggerMode.description: String
    get() = when (this) {
        AlertTriggerMode.TIME -> "Trigger early warnings from the selected ETA window."
        AlertTriggerMode.DISTANCE -> "Trigger early warnings from the selected distance to the destination."
        AlertTriggerMode.BOTH -> "Trigger early warnings from whichever threshold is reached first."
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

private fun Int.toLeadTimeLabel(): String =
    if (this == 0) "Nearby" else "$this min"

private fun Context.appVersionLabel(): String {
    val packageInfo = packageManager.getPackageInfo(packageName, 0)
    val versionName = packageInfo.versionName ?: "unknown"
    return "$versionName (${packageInfo.longVersionCode})"
}
