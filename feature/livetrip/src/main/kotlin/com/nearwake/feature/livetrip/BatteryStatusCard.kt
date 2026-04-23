package com.nearwake.feature.livetrip

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.nearwake.core.ui.NearWakeChipState
import com.nearwake.core.ui.NearWakeSectionHeader
import com.nearwake.core.ui.NearWakeStateChip
import com.nearwake.core.ui.SurfaceCard

@Composable
fun BatteryStatusCard(
    batteryImpact: String,
) {
    SurfaceCard {
        NearWakeSectionHeader(text = "Power mode")
        NearWakeStateChip(
            label = batteryImpact,
            state = when (batteryImpact) {
                "Very Low", "Low" -> NearWakeChipState.Safe
                "Temporary spike" -> NearWakeChipState.Approaching
                else -> NearWakeChipState.Neutral
            },
        )
        Text(
            text = "Monitoring mode adapts automatically so the app stays present without draining the phone unnecessarily.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
