package com.nearwake.feature.livetrip

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nearwake.core.ui.NearWakeCard
import com.nearwake.core.ui.NearWakePrimaryButton
import com.nearwake.core.ui.NearWakeScaffold

@Composable
fun LiveTripScreen(
    onCancel: () -> Unit,
    onSimulateAlert: () -> Unit,
    viewModel: LiveTripViewModel = viewModel(),
) {
    val state = viewModel.state.value
    NearWakeScaffold(
        title = state.destinationName,
        subtitle = "The app is armed and tracking quietly until it needs your attention.",
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onCancel) { Text("Cancel trip") }
            OutlinedButton(onClick = onSimulateAlert) { Text("Test alert") }
        }

        NearWakeCard {
            Text("ETA", style = androidx.compose.material3.MaterialTheme.typography.displayLarge)
            Text(state.etaLabel)
            Text("Elapsed: ${state.elapsedTimeLabel}")
        }

        MonitoringStatusCard(
            monitoringMode = state.monitoringMode,
            confidence = state.confidence,
            alertSummary = state.alertSummary,
        )

        BatteryStatusCard(batteryImpact = state.batteryImpact)

        NearWakePrimaryButton(
            text = "Simulate approach alert",
            onClick = onSimulateAlert,
        )
    }
}
