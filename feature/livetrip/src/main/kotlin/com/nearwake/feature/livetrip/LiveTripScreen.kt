package com.nearwake.feature.livetrip

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
                PulseRing(
                    color = accent,
                    diameter = spacing.massive * 4 + spacing.hero,
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(spacing.sm),
                ) {
                    NearWakeNumericText(
                        text = state.etaLabel.filter { it.isDigit() }.ifBlank { "--" },
                        color = accent,
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
                        text = state.elapsedTimeLabel.ifBlank { "Monitoring quietly in the background" },
                        style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            MonitoringStatusCard(
                monitoringMode = state.monitoringMode,
                confidence = state.confidence,
                alertSummary = state.alertSummary,
                routeSummary = state.routeSummary,
            )

            BatteryStatusCard(batteryImpact = state.batteryImpact)

            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.md),
            ) {
                NearWakeStateChip(
                    label = stateChipLabel(state.monitoringMode),
                    state = stateChipState(state.monitoringMode, state.confidence),
                )
                NearWakeStateChip(
                    label = state.confidence.name.lowercase().replaceFirstChar(Char::uppercase),
                    state = when (state.confidence) {
                        Confidence.HIGH -> NearWakeChipState.Safe
                        Confidence.DEGRADED -> NearWakeChipState.Approaching
                        Confidence.OFFLINE -> NearWakeChipState.Alert
                    },
                )
            }

            NearWakeSecondaryButton(
                modifier = Modifier.fillMaxWidth(),
                text = "Simulate alert",
                onClick = { onSimulateAlert(state.tripId) },
            )
        }
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

private fun stateChipLabel(monitoringMode: MonitoringMode): String = when (monitoringMode) {
    MonitoringMode.GEOFENCE_ONLY -> "Geofence"
    MonitoringMode.BALANCED -> "Balanced"
    MonitoringMode.PRECISE_BURST -> "Burst"
}

private fun stateChipState(
    monitoringMode: MonitoringMode,
    confidence: Confidence,
): NearWakeChipState = when {
    monitoringMode == MonitoringMode.PRECISE_BURST -> NearWakeChipState.Approaching
    confidence == Confidence.HIGH -> NearWakeChipState.Safe
    else -> NearWakeChipState.Monitoring
}
