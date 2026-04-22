package com.nearwake.feature.livetrip

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nearwake.core.ui.NearWakeCard
import com.nearwake.core.ui.NearWakePrimaryButton
import com.nearwake.core.ui.NearWakeScaffold

@Composable
fun LiveTripScreen(
    onCancel: () -> Unit,
    onSimulateAlert: (String) -> Unit,
    viewModel: LiveTripViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    NearWakeScaffold(
        title = state.destinationName,
        subtitle = "The app is armed and tracking quietly until it needs your attention.",
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = { viewModel.cancelTrip(onCancel) }) { Text("Cancel trip") }
            OutlinedButton(onClick = { onSimulateAlert(state.tripId) }) { Text("Test alert") }
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
            onClick = { onSimulateAlert(state.tripId) },
        )
    }
}
