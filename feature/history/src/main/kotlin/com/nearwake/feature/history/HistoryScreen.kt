package com.nearwake.feature.history

import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nearwake.core.ui.NearWakeCard
import com.nearwake.core.ui.NearWakeScaffold

@Composable
fun HistoryScreen(
    onBack: () -> Unit,
    viewModel: HistoryViewModel = viewModel(),
) {
    val state = viewModel.state.value
    NearWakeScaffold(
        title = "Trip history",
        subtitle = "A quick view of the trips you completed or cancelled recently.",
    ) {
        OutlinedButton(onClick = onBack) { Text("Back") }
        state.trips.forEach { trip ->
            NearWakeCard {
                Text(trip)
            }
        }
    }
}
