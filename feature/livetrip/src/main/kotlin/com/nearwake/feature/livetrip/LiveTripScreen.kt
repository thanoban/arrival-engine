package com.nearwake.feature.livetrip

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsTransit
import androidx.compose.material.icons.filled.SignalWifiOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.unit.dp
import com.nearwake.core.designsystem.LocalSpacing
import com.nearwake.core.designsystem.NearWakeColors
import com.nearwake.core.designsystem.NearWakeMotion
import com.nearwake.core.designsystem.ProvideNearWakeStateAccent
import com.nearwake.core.ui.HeroCard
import com.nearwake.core.ui.NearWakeChipState
import com.nearwake.core.ui.NearWakeNumericText
import com.nearwake.core.ui.NearWakeSelectableChip
import com.nearwake.core.ui.NearWakeSecondaryButton
import com.nearwake.core.ui.NearWakeStateChip
import com.nearwake.core.ui.NearWakeTextButton
import com.nearwake.core.ui.PulseRing
import com.nearwake.domain.trip.model.AlertMode
import com.nearwake.domain.trip.model.AlertStage
import com.nearwake.domain.trip.model.Confidence
import com.nearwake.domain.trip.model.MonitoringMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveTripScreen(
    onCancel: () -> Unit,
    onSimulateAlert: (String) -> Unit,
    viewModel: LiveTripViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val spacing = LocalSpacing.current
    var detailsVisible by remember { mutableStateOf(false) }
    val trust = rememberTrustPresentation(
        alertStage = state.alertStage,
        monitoringMode = state.monitoringMode,
        confidence = state.confidence,
    )
    val targetAccent = liveTripAccent(
        monitoringMode = state.monitoringMode,
        alertStage = state.alertStage,
    )
    val accent by animateColorAsState(
        targetValue = targetAccent,
        animationSpec = tween(
            durationMillis = NearWakeMotion.Slow,
            easing = NearWakeMotion.EasingStandard,
        ),
        label = "live-trip-accent",
    )
    val backgroundTint by animateColorAsState(
        targetValue = accent.copy(alpha = 0.04f).compositeOver(NearWakeColors.BgBase),
        animationSpec = tween(
            durationMillis = NearWakeMotion.Slow,
            easing = NearWakeMotion.EasingStandard,
        ),
        label = "live-trip-background",
    )

    ProvideNearWakeStateAccent(accent) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundTint)
            .padding(horizontal = spacing.xl, vertical = spacing.xl),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(spacing.lg),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                NearWakeTextButton(
                    text = "Stop trip",
                    onClick = { viewModel.cancelTrip(onCancel) },
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    modifier = Modifier.semantics(mergeDescendants = true) {
                        contentDescription = buildString {
                            append(state.destinationName)
                            append(". ETA ")
                            append(state.etaLabel)
                            append(". ")
                            append(trust.heroMessage)
                        }
                    },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(spacing.md),
                ) {
                    Text(
                        text = state.destinationName,
                        style = MaterialTheme.typography.titleMedium,
                        color = NearWakeColors.TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                    )
                    BoxWithConstraints(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                    PulseRing(
                        color = accent,
                        diameter = 256.dp,
                    )
                    Row(
                        modifier = Modifier.semantics {
                            liveRegion = LiveRegionMode.Polite
                            contentDescription = buildString {
                                append("ETA ")
                                append(state.etaLabel)
                                append(" to ")
                                append(state.destinationName)
                            }
                        },
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(spacing.xs),
                    ) {
                        NearWakeNumericText(
                            text = state.etaLabel.filter { it.isDigit() }.ifBlank { "--" },
                            color = NearWakeColors.TextPrimary,
                            style = MaterialTheme.typography.displayMedium,
                        )
                        Text(
                            text = "min",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                }
                }
            }

            LiveTripStatusStrip(
                alertStage = state.alertStage,
                confidence = state.confidence,
                batterySaverActive = state.batterySaverActive,
            )

            TransferProgressCard(
                transferSteps = state.transferSteps,
            )

            if (trust.biasEarlyMessage != null) {
                HeroCard(accent = trust.biasCardAccent) {
                    NearWakeStateChip(
                        label = trust.biasLabel,
                        state = trust.biasChipState,
                    )
                    Text(
                        text = trust.biasEarlyMessage,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                NearWakeTextButton(
                    text = "Details",
                    onClick = { detailsVisible = true },
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.sm),
            ) {
                AlertMode.entries.forEach { mode ->
                    NearWakeSelectableChip(
                        selected = state.alertMode == mode,
                        label = when (mode) {
                            AlertMode.ACTIVE -> "Active"
                            AlertMode.SLEEP -> "Sleep"
                        },
                        onClick = { viewModel.updateAlertMode(mode) },
                    )
                }
            }

            NearWakeSecondaryButton(
                modifier = Modifier.fillMaxWidth(),
                text = "Simulate alert",
                onClick = { onSimulateAlert(state.tripId) },
            )
        }
    }

    if (detailsVisible) {
        ModalBottomSheet(
            onDismissRequest = { detailsVisible = false },
            containerColor = NearWakeColors.BgSurface,
        ) {
            LiveTripDetailsSheet(
                monitoringMode = state.monitoringMode,
                confidence = state.confidence,
                routeSummary = state.routeSummary,
                alertSummary = state.alertSummary,
                batteryImpact = state.batteryImpact,
                elapsedTimeLabel = state.elapsedTimeLabel,
            )
        }
    }
    } // ProvideNearWakeStateAccent
}

@Composable
private fun LiveTripStatusStrip(
    alertStage: AlertStage,
    confidence: Confidence,
    batterySaverActive: Boolean,
) {
    val spacing = LocalSpacing.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(32.dp)
            .semantics(mergeDescendants = true) {
                contentDescription = buildString {
                    append("Trip status. ")
                    append(
                        when (alertStage) {
                            AlertStage.MONITORING -> "Monitoring"
                            AlertStage.APPROACH -> "Approach window"
                            AlertStage.IMMINENT -> "Imminent stop"
                            AlertStage.ARRIVAL -> "Arriving now"
                            AlertStage.RECOVERY -> "Recovery mode"
                        },
                    )
                    append(". Signal ")
                    append(
                        when (confidence) {
                            Confidence.HIGH -> "high"
                            Confidence.DEGRADED -> "degraded"
                            Confidence.OFFLINE -> "offline"
                        },
                    )
                    if (batterySaverActive) {
                        append(". Battery saver active")
                    }
                }
            },
        horizontalArrangement = Arrangement.spacedBy(spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = when (alertStage) {
                AlertStage.MONITORING -> Icons.Filled.DirectionsTransit
                AlertStage.APPROACH -> Icons.Filled.DirectionsTransit
                AlertStage.IMMINENT -> Icons.Filled.Warning
                AlertStage.ARRIVAL -> Icons.Filled.CheckCircle
                AlertStage.RECOVERY -> Icons.Filled.Warning
            },
            contentDescription = null,
            tint = when (alertStage) {
                AlertStage.MONITORING -> NearWakeColors.MonitoringBase
                AlertStage.APPROACH -> NearWakeColors.ApproachBase
                AlertStage.IMMINENT, AlertStage.RECOVERY -> NearWakeColors.AlertBase
                AlertStage.ARRIVAL -> NearWakeColors.SafeBase
            },
        )
        if (confidence == Confidence.DEGRADED) {
            StatusStripLabel(
                icon = Icons.Filled.Warning,
                label = "Signal degraded",
                tint = NearWakeColors.ApproachBase,
            )
        }
        if (confidence == Confidence.OFFLINE) {
            StatusStripLabel(
                icon = Icons.Filled.SignalWifiOff,
                label = "Underground",
                tint = NearWakeColors.AlertBase,
            )
        }
        if (batterySaverActive) {
            StatusStripLabel(
                icon = Icons.Filled.BatterySaver,
                label = "Low battery",
                tint = NearWakeColors.ApproachBase,
            )
        }
    }
}

@Composable
private fun StatusStripLabel(
    icon: ImageVector,
    label: String,
    tint: Color,
) {
    val spacing = LocalSpacing.current
    Row(
        horizontalArrangement = Arrangement.spacedBy(spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = tint,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = NearWakeColors.TextSecondary,
        )
    }
}

@Composable
private fun LiveTripDetailsSheet(
    monitoringMode: MonitoringMode,
    confidence: Confidence,
    routeSummary: String,
    alertSummary: String,
    batteryImpact: String,
    elapsedTimeLabel: String,
) {
    val spacing = LocalSpacing.current
    val colors = listOf(
        "Monitoring mode" to when (monitoringMode) {
            MonitoringMode.GEOFENCE_ONLY -> "Low power"
            MonitoringMode.BALANCED -> "Balanced"
            MonitoringMode.PRECISE_BURST -> "Precise"
        },
        "Signal quality" to when (confidence) {
            Confidence.HIGH -> "High"
            Confidence.DEGRADED -> "Degraded"
            Confidence.OFFLINE -> "Offline"
        },
        "Power impact" to batteryImpact,
        "Elapsed" to elapsedTimeLabel.ifBlank { "Just started" },
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.xl, vertical = spacing.md),
        verticalArrangement = Arrangement.spacedBy(spacing.md),
    ) {
        Text(
            text = "Trip details",
            style = MaterialTheme.typography.titleLarge,
            color = NearWakeColors.TextPrimary,
        )
        colors.forEach { (label, value) ->
            Surface(
                modifier = Modifier.semantics {
                    contentDescription = "$label. $value"
                },
                color = NearWakeColors.BgElevated,
                border = BorderStroke(1.dp, NearWakeColors.BorderSubtle),
                shape = CircleShape,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = spacing.md, vertical = spacing.sm),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = NearWakeColors.TextSecondary,
                    )
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodyMedium,
                        color = NearWakeColors.TextPrimary,
                    )
                }
            }
        }
        Text(
            text = routeSummary,
            modifier = Modifier.semantics {
                contentDescription = "Route summary. $routeSummary"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = NearWakeColors.TextPrimary,
        )
        if (alertSummary.isNotBlank()) {
            Text(
                text = alertSummary,
                modifier = Modifier.semantics {
                    contentDescription = "Alert summary. $alertSummary"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = NearWakeColors.TextSecondary,
            )
        }
        Spacer(modifier = Modifier.height(spacing.xl))
    }
}

private data class TrustPresentation(
    val heroMessage: String,
    val biasLabel: String,
    val biasChipState: NearWakeChipState,
    val biasCardAccent: Color,
    val biasEarlyMessage: String?,
)

@Composable
private fun rememberTrustPresentation(
    alertStage: AlertStage,
    monitoringMode: MonitoringMode,
    confidence: Confidence,
): TrustPresentation {
    return when (confidence) {
        Confidence.HIGH -> TrustPresentation(
            heroMessage = when (alertStage) {
                AlertStage.MONITORING -> "Tracking quietly while you ride."
                AlertStage.APPROACH -> "NearWake has entered the approach window."
                AlertStage.IMMINENT -> "Your stop is close. Get ready to move."
                AlertStage.ARRIVAL -> "It is time to exit now."
                AlertStage.RECOVERY -> "NearWake is helping you recover the missed stop."
            },
            biasLabel = "On track",
            biasChipState = NearWakeChipState.Safe,
            biasCardAccent = NearWakeColors.SafeBase,
            biasEarlyMessage = null,
        )

        Confidence.DEGRADED -> TrustPresentation(
            heroMessage = when (alertStage) {
                AlertStage.MONITORING -> "Monitoring closely and biasing earlier."
                AlertStage.APPROACH -> "Approach started a little early to stay safe."
                AlertStage.IMMINENT -> "Your stop is close, so NearWake is leaning conservative."
                AlertStage.ARRIVAL -> "NearWake is treating this as arrival now."
                AlertStage.RECOVERY -> "Recovery mode is active with conservative timing."
            },
            biasLabel = "Alerting earlier",
            biasChipState = NearWakeChipState.Approaching,
            biasCardAccent = NearWakeColors.ApproachBase,
            biasEarlyMessage = "One signal has weakened, so NearWake will warn earlier to stay conservative.",
        )

        Confidence.OFFLINE -> TrustPresentation(
            heroMessage = when (alertStage) {
                AlertStage.MONITORING -> "Signal dropped, but NearWake is still guarding your stop."
                AlertStage.APPROACH -> "Signal is weak, so NearWake moved into approach early."
                AlertStage.IMMINENT -> "NearWake is treating this like an imminent stop to stay safe."
                AlertStage.ARRIVAL -> "NearWake is assuming arrival now because confidence is low."
                AlertStage.RECOVERY -> "Recovery mode is active while signal stays weak."
            },
            biasLabel = "Alerting much earlier",
            biasChipState = NearWakeChipState.Alert,
            biasCardAccent = NearWakeColors.AlertBase,
            biasEarlyMessage = "Location confidence is low, so NearWake has switched to underground-safe behavior and will alert much earlier.",
        )
    }
}

private fun liveTripAccent(
    monitoringMode: MonitoringMode,
    alertStage: AlertStage,
): Color {
    return when {
        alertStage == AlertStage.IMMINENT || alertStage == AlertStage.ARRIVAL -> NearWakeColors.AlertBase
        alertStage == AlertStage.APPROACH -> NearWakeColors.ApproachBase
        monitoringMode == MonitoringMode.PRECISE_BURST -> NearWakeColors.ApproachBase
        else -> NearWakeColors.MonitoringBase
    }
}
