package com.nearwake.feature.livetrip

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.nearwake.core.ui.NearWakeCard
import com.nearwake.domain.trip.model.Confidence
import com.nearwake.domain.trip.model.MonitoringMode

@Composable
fun MonitoringStatusCard(
    monitoringMode: MonitoringMode,
    confidence: Confidence,
    alertSummary: String,
) {
    NearWakeCard {
        Text("Status", style = MaterialTheme.typography.titleLarge)
        Text("Mode: ${monitoringMode.name}")
        Text("Confidence: ${confidence.name}")
        Text("Alert: $alertSummary")
    }
}
