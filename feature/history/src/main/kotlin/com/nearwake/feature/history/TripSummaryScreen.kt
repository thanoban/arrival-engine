package com.nearwake.feature.history

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.nearwake.core.ui.NearWakeScaffold

@Composable
fun TripSummaryScreen() {
    NearWakeScaffold(
        title = "Trip summary",
        subtitle = "Detailed trip breakdown will land here in a later pass.",
    ) {
        Text("Summary placeholder")
    }
}
