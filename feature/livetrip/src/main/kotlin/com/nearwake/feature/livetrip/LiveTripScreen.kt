package com.nearwake.feature.livetrip

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.compositeOver
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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

@Composable
fun LiveTripScreen(
    onCancel: () -> Unit,
    onSimulateAlert: (String) -> Unit,
    viewModel: LiveTripViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val spacing = LocalSpacing.current
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
            androidx.compose.foundation.layout.Row(
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
                BoxWithConstraints(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    PulseRing(
                        color = accent,
                        diameter = maxWidth,
                    )
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(spacing.sm),
                    ) {
                        NearWakeNumericText(
                            text = state.etaLabel.filter { it.isDigit() }.ifBlank { "--" },
                            color = NearWakeColors.TextPrimary,
                            style = androidx.compose.material3.MaterialTheme.typography.displayMedium,
                        )
                        Text(
                            text = "min",
                            style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onBackground,
                        )
                        Text(
                            text = state.destinationName,
                            style = androidx.compose.material3.MaterialTheme.typography.headlineLarge,
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onBackground,
                        )
                        Text(
                            text = trust.heroMessage,
                            style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            MonitoringStatusCard(
                etaLabel = state.etaLabel,
                monitoringMode = state.monitoringMode,
                confidence = state.confidence,
                alertSummary = state.alertSummary,
                routeSummary = state.routeSummary,
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
                        style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onBackground,
                    )
                }
            }

            BatteryStatusCard(batteryImpact = state.batteryImpact)

            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.md),
            ) {
                NearWakeStateChip(
                    label = trust.stageLabel,
                    state = trust.stageChipState,
                )
                NearWakeStateChip(
                    label = trust.confidenceLabel,
                    state = trust.confidenceChipState,
                )
                if (trust.showUndergroundChip) {
                    NearWakeStateChip(
                        label = "Underground mode",
                        state = NearWakeChipState.Alert,
                    )
                }
            }

            androidx.compose.foundation.layout.Row(
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
    } // ProvideNearWakeStateAccent
}

private data class TrustPresentation(
    val stageLabel: String,
    val stageChipState: NearWakeChipState,
    val confidenceLabel: String,
    val confidenceChipState: NearWakeChipState,
    val heroMessage: String,
    val biasLabel: String,
    val biasChipState: NearWakeChipState,
    val biasCardAccent: androidx.compose.ui.graphics.Color,
    val biasEarlyMessage: String?,
    val showUndergroundChip: Boolean,
)

@Composable
private fun rememberTrustPresentation(
    alertStage: AlertStage,
    monitoringMode: MonitoringMode,
    confidence: Confidence,
): TrustPresentation {
    val stageLabel = when (alertStage) {
        AlertStage.MONITORING -> "Monitoring"
        AlertStage.APPROACH -> "Approach"
        AlertStage.IMMINENT -> "Imminent"
        AlertStage.ARRIVAL -> "Arrival"
        AlertStage.RECOVERY -> "Recovery"
    }
    val stageChipState = when (stageLabel) {
        "Arrival" -> NearWakeChipState.Alert
        "Imminent" -> NearWakeChipState.Alert
        "Approaching" -> NearWakeChipState.Approaching
        "Approach" -> NearWakeChipState.Approaching
        "Recovery" -> NearWakeChipState.Approaching
        else -> NearWakeChipState.Monitoring
    }
    return when (confidence) {
        Confidence.HIGH -> TrustPresentation(
            stageLabel = stageLabel,
            stageChipState = stageChipState,
            confidenceLabel = "High confidence",
            confidenceChipState = NearWakeChipState.Safe,
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
            showUndergroundChip = false,
        )

        Confidence.DEGRADED -> TrustPresentation(
            stageLabel = stageLabel,
            stageChipState = stageChipState,
            confidenceLabel = "Medium confidence",
            confidenceChipState = NearWakeChipState.Approaching,
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
            showUndergroundChip = false,
        )

        Confidence.OFFLINE -> TrustPresentation(
            stageLabel = if (stageLabel == "Monitoring") "Approaching" else stageLabel,
            stageChipState = NearWakeChipState.Approaching,
            confidenceLabel = "Low confidence",
            confidenceChipState = NearWakeChipState.Alert,
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
            showUndergroundChip = true,
        )
    }
}

private fun liveTripAccent(
    monitoringMode: MonitoringMode,
    alertStage: AlertStage,
): androidx.compose.ui.graphics.Color {
    return when {
        alertStage == AlertStage.IMMINENT || alertStage == AlertStage.ARRIVAL -> NearWakeColors.AlertBase
        alertStage == AlertStage.APPROACH -> NearWakeColors.ApproachBase
        monitoringMode == MonitoringMode.PRECISE_BURST -> NearWakeColors.ApproachBase
        else -> NearWakeColors.MonitoringBase
    }
}
