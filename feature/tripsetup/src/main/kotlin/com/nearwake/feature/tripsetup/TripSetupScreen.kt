package com.nearwake.feature.tripsetup

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsTransit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nearwake.core.designsystem.LocalNearWakeColors
import com.nearwake.core.designsystem.LocalSpacing
import com.nearwake.core.designsystem.NearWakeColors
import com.nearwake.core.designsystem.ProvideNearWakeStateAccent
import com.nearwake.core.ui.ElevatedCard
import com.nearwake.core.ui.NearWakeButtonSize
import com.nearwake.core.ui.NearWakeChipState
import com.nearwake.core.ui.NearWakePrimaryButton
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
fun TripSetupScreen(
    onStartTrip: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: TripSetupViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val spacing = LocalSpacing.current
    val colors = LocalNearWakeColors.current
    val routeReady = state.etaLabel.startsWith("~")
    var advancedExpanded by rememberSaveable { mutableStateOf(false) }

    ProvideNearWakeStateAccent(NearWakeColors.SafeBase) {
        Scaffold(
            containerColor = colors.bgBase,
            bottomBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.bgBase)
                        .padding(horizontal = spacing.lg, vertical = spacing.md),
                ) {
                    NearWakePrimaryButton(
                        modifier = Modifier.fillMaxWidth(),
                        text = "Arm trip",
                        onClick = { viewModel.startTrip(onStartTrip) },
                        enabled = state.canStart,
                        size = NearWakeButtonSize.Large,
                    )
                }
            },
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(innerPadding)
                    .padding(horizontal = spacing.xl, vertical = spacing.xl),
                verticalArrangement = Arrangement.spacedBy(spacing.md),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    NearWakeTextButton(text = "Back", onClick = onBack)
                }

                ElevatedCard(
                    modifier = Modifier.heightIn(min = 72.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Place,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = colors.monitoringBase,
                        )
                        Spacer(modifier = Modifier.width(spacing.md))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = state.destinationName,
                                style = MaterialTheme.typography.bodyLarge,
                                color = colors.textPrimary,
                            )
                            if (state.destinationAddress.isNotBlank()) {
                                Text(
                                    text = state.destinationAddress,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.textSecondary,
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics {
                            contentDescription = if (routeReady) {
                                "Route preview. ${state.routeSummary}. ETA ${state.etaLabel}."
                            } else {
                                "Destination-only monitoring fallback. ${state.routeSummary}."
                            }
                        }
                        .height(40.dp),
                    horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Filled.DirectionsTransit,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = colors.monitoringBase,
                    )
                    Text(
                        text = if (routeReady) state.routeSummary else "Destination-only monitoring",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textPrimary,
                        modifier = Modifier.weight(1f),
                    )
                    NearWakeStateChip(
                        label = if (routeReady) state.etaLabel else "Fallback",
                        state = if (routeReady) NearWakeChipState.Safe else NearWakeChipState.Neutral,
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
                    NearWakeSectionHeader(text = "Trigger")
                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        AlertTriggerMode.entries.forEachIndexed { index, triggerMode ->
                            SegmentedButton(
                                selected = state.alertTriggerMode == triggerMode,
                                onClick = { viewModel.selectAlertTriggerMode(triggerMode) },
                                shape = androidx.compose.material3.SegmentedButtonDefaults.itemShape(
                                    index = index,
                                    count = AlertTriggerMode.entries.size,
                                ),
                                icon = {
                                    Icon(
                                        imageVector = when (triggerMode) {
                                            AlertTriggerMode.TIME -> Icons.Filled.Schedule
                                            AlertTriggerMode.DISTANCE -> Icons.Filled.MyLocation
                                            AlertTriggerMode.BOTH -> Icons.Filled.Tune
                                        },
                                        contentDescription = null,
                                    )
                                },
                                label = { Text(triggerMode.label) },
                            )
                        }
                    }
                    Text(
                        text = state.alertTriggerMode.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textSecondary,
                    )
                }

                if (state.alertTriggerMode.usesTimeTrigger()) {
                    Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
                        NearWakeSectionHeader(text = "Alert Before Arrival")
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                        ) {
                            NearWakeSelectableChip(
                                selected = state.alertLeadMinutes == 0,
                                label = "Nearby",
                                onClick = { viewModel.selectLeadMinutes(0) },
                            )
                            listOf(2, 5, 10, 15).forEach { minutes ->
                                NearWakeSelectableChip(
                                    selected = state.alertLeadMinutes == minutes,
                                    label = "$minutes min",
                                    onClick = { viewModel.selectLeadMinutes(minutes) },
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics {
                            role = Role.Button
                            stateDescription = if (advancedExpanded) "Expanded" else "Collapsed"
                            contentDescription = "Advanced trip options"
                        }
                        .clickable { advancedExpanded = !advancedExpanded },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Tune,
                        contentDescription = null,
                        tint = colors.textSecondary,
                    )
                    Text(
                        text = "Advanced",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textSecondary,
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        imageVector = if (advancedExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = null,
                        tint = colors.textSecondary,
                    )
                }

                AnimatedVisibility(visible = advancedExpanded) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(spacing.md),
                    ) {
                        if (state.alertTriggerMode.usesDistanceTrigger()) {
                            Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
                                NearWakeSectionHeader(text = "Distance Trigger")
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                                    verticalArrangement = Arrangement.spacedBy(spacing.sm),
                                ) {
                                    listOf(500, 1000, 1500, 2000).forEach { distanceMeters ->
                                        NearWakeSelectableChip(
                                            selected = state.alertDistanceMeters == distanceMeters,
                                            label = distanceMeters.toDistanceLabel(),
                                            onClick = { viewModel.selectAlertDistanceMeters(distanceMeters) },
                                        )
                                    }
                                }
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
                            NearWakeSectionHeader(text = "Alert Style")
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                                verticalArrangement = Arrangement.spacedBy(spacing.sm),
                            ) {
                                AlertIntensity.entries.forEach { intensity ->
                                    NearWakeSelectableChip(
                                        selected = state.alertIntensity == intensity,
                                        label = intensity.name.lowercase().replaceFirstChar(Char::uppercase),
                                        onClick = { viewModel.selectIntensity(intensity) },
                                    )
                                }
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
                            NearWakeSectionHeader(text = "Trip Mode")
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                                verticalArrangement = Arrangement.spacedBy(spacing.sm),
                            ) {
                                AlertMode.entries.forEach { mode ->
                                    NearWakeSelectableChip(
                                        selected = state.alertMode == mode,
                                        label = mode.label,
                                        onClick = { viewModel.selectAlertMode(mode) },
                                    )
                                }
                            }
                            Text(
                                text = when (state.alertMode) {
                                    AlertMode.ACTIVE -> "Active mode keeps alerts concise while you stay awake and distracted."
                                    AlertMode.SLEEP -> "Sleep mode prepares stronger alerts for naps and locked-screen travel."
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.textSecondary,
                            )
                        }

                        SurfaceCard(compact = true) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = spacing.md),
                                    verticalArrangement = Arrangement.spacedBy(spacing.xs),
                                ) {
                                    Text(
                                        text = "Background monitoring",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = colors.textPrimary,
                                    )
                                    Text(
                                        text = "Keeps the service alive while you lock the screen or switch apps.",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = colors.textSecondary,
                                    )
                                }
                                Switch(
                                    checked = state.backgroundMonitoringEnabled,
                                    onCheckedChange = viewModel::setBackgroundMonitoringEnabled,
                                )
                            }
                        }
                    }
                }
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

private val AlertTriggerMode.description: String
    get() = when (this) {
        AlertTriggerMode.TIME -> "Trigger early warnings from the selected ETA window."
        AlertTriggerMode.DISTANCE -> "Trigger early warnings from the selected distance to the destination."
        AlertTriggerMode.BOTH -> "Trigger early warnings from whichever threshold is reached first."
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
