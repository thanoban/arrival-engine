package com.nearwake.feature.alerts

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nearwake.core.ui.NearWakePrimaryButton
import com.nearwake.core.ui.NearWakeScaffold

@Composable
fun RecoveryScreen(
    onEndTrip: () -> Unit,
    viewModel: RecoveryViewModel = viewModel(),
) {
    val state = viewModel.state.value
    NearWakeScaffold(
        title = "You may have missed your stop",
        subtitle = "${state.destinationName} was ${state.missedByLabel}",
    ) {
        Text(
            text = "Recovery mode is here so you can end the trip quickly and re-orient yourself.",
            style = MaterialTheme.typography.bodyLarge,
        )
        NearWakePrimaryButton(
            text = "End trip",
            onClick = onEndTrip,
        )
    }
}
