package com.nearwake.feature.livetrip

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsTransit
import androidx.compose.material.icons.filled.TransferWithinAStation
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nearwake.core.designsystem.LocalNearWakeColors
import com.nearwake.core.designsystem.LocalSpacing
import com.nearwake.core.ui.ElevatedCard
import com.nearwake.core.ui.NearWakeSectionHeader
import com.nearwake.core.ui.SurfaceCard
import com.nearwake.domain.routing.model.RouteSignalQuality

@Composable
fun TransferProgressCard(
    transferSteps: List<TransferProgressUiState>,
) {
    val spacing = LocalSpacing.current
    val colors = LocalNearWakeColors.current
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
            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
        ) {
            transferSteps.forEach { step ->
                SurfaceCard(
                    modifier = Modifier
                        .width(160.dp)
                        .height(64.dp),
                    compact = true,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                    ) {
                        Icon(
                            imageVector = if (step.status == TransferProgressStatus.Final) {
                                Icons.Filled.DirectionsTransit
                            } else {
                                Icons.Filled.TransferWithinAStation
                            },
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = colors.monitoringBase,
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = step.title,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground,
                                maxLines = 1,
                            )
                            Text(
                                text = step.timingLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(step.signalQuality.dotColor(), CircleShape),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RouteSignalQuality.dotColor() = when (this) {
    RouteSignalQuality.HIGH -> LocalNearWakeColors.current.safeBase
    RouteSignalQuality.DEGRADED -> LocalNearWakeColors.current.approachBase
    RouteSignalQuality.OFFLINE -> LocalNearWakeColors.current.alertBase
}
