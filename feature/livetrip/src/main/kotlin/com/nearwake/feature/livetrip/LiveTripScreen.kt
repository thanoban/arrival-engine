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
import com.nearwake.core.ui.HeroCard
import com.nearwake.core.ui.NearWakeChipState
import com.nearwake.core.ui.NearWakeNumericText
import com.nearwake.core.ui.NearWakeSecondaryButton
import com.nearwake.core.ui.NearWakeStateChip
import com.nearwake.core.ui.NearWakeTextButton
import com.nearwake.core.ui.PulseRing
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
        etaLabel = state.etaLabel,
        monitoringMode = state.monitoringMode,
        confidence = state.confidence,
    )
    val targetAccent = liveTripAccent(
        monitoringMode = state.monitoringMode,
        etaLabel = state.etaLabel,
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

            NearWakeSecondaryButton(
                modifier = Modifier.fillMaxWidth(),
                text = "Simulate alert",
                onClick = { onSimulateAlert(state.tripId) },
            )
        }
    }
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
    etaLabel: String,
    monitoringMode: MonitoringMode,
    confidence: Confidence,
): TrustPresentation {
    val etaMinutes = etaLabel.filter { it.isDigit() }.toIntOrNull()
    val stageLabel = when {
        etaMinutes != null && etaMinutes <= 1 -> "Arrival"
        etaMinutes != null && etaMinutes <= 10 -> "Approaching"
        monitoringMode == MonitoringMode.PRECISE_BURST -> "Approaching"
        else -> "Monitoring"
    }
    val stageChipState = when (stageLabel) {
        "Arrival" -> NearWakeChipState.Alert
        "Approaching" -> NearWakeChipState.Approaching
        else -> NearWakeChipState.Monitoring
    }
    return when (confidence) {
        Confidence.HIGH -> TrustPresentation(
            stageLabel = stageLabel,
            stageChipState = stageChipState,
            confidenceLabel = "High confidence",
            confidenceChipState = NearWakeChipState.Safe,
            heroMessage = "Tracking quietly while you ride.",
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
            heroMessage = "Monitoring closely and biasing earlier.",
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
            heroMessage = "Signal dropped, but NearWake is still guarding your stop.",
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
    etaLabel: String,
): androidx.compose.ui.graphics.Color {
    val etaMinutes = etaLabel.filter { it.isDigit() }.toIntOrNull()
    return when {
        etaMinutes != null && etaMinutes <= 10 -> NearWakeColors.ApproachBase
        monitoringMode == MonitoringMode.PRECISE_BURST -> NearWakeColors.ApproachBase
        else -> NearWakeColors.MonitoringBase
    }
}
