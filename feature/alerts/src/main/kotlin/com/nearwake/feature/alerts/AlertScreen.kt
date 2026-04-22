package com.nearwake.feature.alerts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nearwake.core.ui.NearWakePrimaryButton
import com.nearwake.core.ui.NearWakeScaffold

@Composable
fun AlertScreen(
    onDismiss: () -> Unit,
    onRecovery: () -> Unit,
    viewModel: AlertViewModel = viewModel(),
) {
    val state = viewModel.state.value
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
            onClick = onDismiss,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onRecovery) { Text("Recovery") }
            OutlinedButton(onClick = onDismiss) { Text("End trip") }
        }
    }
}
