package com.nearwake.feature.alerts

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nearwake.core.ui.NearWakePrimaryButton
import com.nearwake.core.ui.NearWakeScaffold

@Composable
fun RecoveryScreen(
    onEndTrip: () -> Unit,
    viewModel: RecoveryViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
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
            onClick = { viewModel.endTrip(onEndTrip) },
        )
    }
}
