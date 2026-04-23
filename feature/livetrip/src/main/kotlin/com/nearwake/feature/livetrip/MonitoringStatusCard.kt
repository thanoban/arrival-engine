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
    monitoringMode: MonitoringMode,
    confidence: Confidence,
    alertSummary: String,
    routeSummary: String,
) {
    ElevatedCard {
        NearWakeSectionHeader(text = "Monitoring")
        NearWakeStatusRow {
            NearWakeStateChip(
                label = monitoringMode.name.replace('_', ' ').lowercase().replaceFirstChar(Char::uppercase),
                state = when (monitoringMode) {
                    MonitoringMode.GEOFENCE_ONLY -> NearWakeChipState.Monitoring
                    MonitoringMode.BALANCED -> NearWakeChipState.Monitoring
                    MonitoringMode.PRECISE_BURST -> NearWakeChipState.Approaching
                },
            )
            NearWakeStateChip(
                label = "${confidence.name.lowercase().replaceFirstChar(Char::uppercase)} confidence",
                state = when (confidence) {
                    Confidence.HIGH -> NearWakeChipState.Safe
                    Confidence.DEGRADED -> NearWakeChipState.Approaching
                    Confidence.OFFLINE -> NearWakeChipState.Alert
                },
            )
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
    }
}
