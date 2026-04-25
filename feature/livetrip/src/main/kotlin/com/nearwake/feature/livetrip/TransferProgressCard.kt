package com.nearwake.feature.livetrip

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nearwake.core.ui.ElevatedCard
import com.nearwake.core.ui.NearWakeChipState
import com.nearwake.core.ui.NearWakeSectionHeader
import com.nearwake.core.ui.NearWakeStateChip
import com.nearwake.core.ui.SurfaceCard
import com.nearwake.domain.routing.model.RouteSignalQuality

@Composable
fun TransferProgressCard(
    transferSteps: List<TransferProgressUiState>,
) {
    if (transferSteps.isEmpty()) {
        SurfaceCard {
            NearWakeSectionHeader(text = "Route legs")
            Text(
                text = "NearWake is using destination-only monitoring for this trip.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }

    ElevatedCard {
        NearWakeSectionHeader(text = "Route legs")
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            transferSteps.forEach { step ->
                SurfaceCard(modifier = Modifier.width(220.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        NearWakeStateChip(
                            label = step.timingLabel,
                            state = when (step.status) {
                                TransferProgressStatus.Completed -> NearWakeChipState.Neutral
                                TransferProgressStatus.Soon -> NearWakeChipState.Approaching
                                TransferProgressStatus.Upcoming -> NearWakeChipState.Monitoring
                                TransferProgressStatus.Final -> NearWakeChipState.Safe
                            },
                        )
                        if (step.signalQuality != RouteSignalQuality.HIGH) {
                            NearWakeStateChip(
                                label = when (step.signalQuality) {
                                    RouteSignalQuality.DEGRADED -> "Medium"
                                    RouteSignalQuality.OFFLINE -> "Low signal"
                                    RouteSignalQuality.HIGH -> ""
                                },
                                state = when (step.signalQuality) {
                                    RouteSignalQuality.DEGRADED -> NearWakeChipState.Approaching
                                    RouteSignalQuality.OFFLINE -> NearWakeChipState.Alert
                                    RouteSignalQuality.HIGH -> NearWakeChipState.Safe
                                },
                            )
                        }
                    }
                    Text(
                        text = step.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Text(
                        text = step.subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
