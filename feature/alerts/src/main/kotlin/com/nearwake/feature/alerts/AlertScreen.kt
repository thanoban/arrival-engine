package com.nearwake.feature.alerts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nearwake.core.ui.NearWakePrimaryButton
import com.nearwake.core.ui.NearWakeScaffold

@Composable
fun AlertScreen(
    onDismiss: () -> Unit,
    onRecovery: (String) -> Unit,
    viewModel: AlertViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    NearWakeScaffold(
        title = "Approaching your stop",
        subtitle = state.destinationName,
    ) {
        Text(
            text = state.etaLabel,
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.error,
        )
        NearWakePrimaryButton(
            text = "Dismiss",
            onClick = { viewModel.dismissTrip(onDismiss) },
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = { onRecovery(state.tripId) }) { Text("Recovery") }
            OutlinedButton(onClick = { viewModel.dismissTrip(onDismiss) }) { Text("End trip") }
        }
    }
}
