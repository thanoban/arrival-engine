package com.nearwake.feature.livetrip

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.nearwake.core.ui.ElevatedCard
import com.nearwake.core.ui.NearWakeChipState
import com.nearwake.core.ui.NearWakeSectionHeader
import com.nearwake.core.ui.NearWakeStateChip
import com.nearwake.core.ui.NearWakeStatusRow
import com.nearwake.domain.trip.model.Confidence
import com.nearwake.domain.trip.model.MonitoringMode

@Composable
fun MonitoringStatusCard(
    etaLabel: String,
    monitoringMode: MonitoringMode,
    confidence: Confidence,
    alertSummary: String,
    routeSummary: String,
) {
    val etaMinutes = etaLabel.filter { it.isDigit() }.toIntOrNull()
    val stageLabel = when {
        etaMinutes != null && etaMinutes <= 1 -> "Arrival window"
        etaMinutes != null && etaMinutes <= 10 -> "Approach window"
        monitoringMode == MonitoringMode.PRECISE_BURST -> "Approach window"
        else -> "Monitoring"
    }
    ElevatedCard {
        NearWakeSectionHeader(text = "Monitoring")
        NearWakeStatusRow {
            NearWakeStateChip(
                label = stageLabel,
                state = when (monitoringMode) {
                    MonitoringMode.GEOFENCE_ONLY -> {
                        if (etaMinutes != null && etaMinutes <= 1) NearWakeChipState.Alert else NearWakeChipState.Monitoring
                    }
                    MonitoringMode.BALANCED -> {
                        if (etaMinutes != null && etaMinutes <= 1) NearWakeChipState.Alert else NearWakeChipState.Monitoring
                    }
                    MonitoringMode.PRECISE_BURST -> {
                        if (etaMinutes != null && etaMinutes <= 1) NearWakeChipState.Alert else NearWakeChipState.Approaching
                    }
                },
            )
            NearWakeStateChip(
                label = when (confidence) {
                    Confidence.HIGH -> "High confidence"
                    Confidence.DEGRADED -> "Medium confidence"
                    Confidence.OFFLINE -> "Low confidence"
                },
                state = when (confidence) {
                    Confidence.HIGH -> NearWakeChipState.Safe
                    Confidence.DEGRADED -> NearWakeChipState.Approaching
                    Confidence.OFFLINE -> NearWakeChipState.Alert
                },
            )
            NearWakeStateChip(
                label = when (monitoringMode) {
                    MonitoringMode.GEOFENCE_ONLY -> "Low power"
                    MonitoringMode.BALANCED -> "Balanced"
                    MonitoringMode.PRECISE_BURST -> "Precise"
                },
                state = when (monitoringMode) {
                    MonitoringMode.GEOFENCE_ONLY -> NearWakeChipState.Neutral
                    MonitoringMode.BALANCED -> NearWakeChipState.Monitoring
                    MonitoringMode.PRECISE_BURST -> NearWakeChipState.Approaching
                },
            )
            if (confidence == Confidence.OFFLINE) {
                NearWakeStateChip(
                    label = "Underground mode",
                    state = NearWakeChipState.Alert,
                )
            }
        }
        Text(
            text = routeSummary,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        if (alertSummary.isNotBlank()) {
            Text(
                text = alertSummary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (confidence != Confidence.HIGH) {
            Text(
                text = when (confidence) {
                    Confidence.HIGH -> ""
                    Confidence.DEGRADED -> "NearWake is biasing earlier because one monitoring signal weakened."
                    Confidence.OFFLINE -> "Signal confidence is low, so NearWake has switched to an earlier, more conservative alert path."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
