package com.nearwake.feature.livetrip

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.nearwake.core.ui.NearWakeCard

@Composable
fun BatteryStatusCard(
    batteryImpact: String,
) {
    NearWakeCard {
        Text("Battery impact", style = MaterialTheme.typography.titleLarge)
        Text(batteryImpact)
    }
}
