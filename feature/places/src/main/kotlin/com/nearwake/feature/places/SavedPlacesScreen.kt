package com.nearwake.feature.places

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.nearwake.core.ui.NearWakeScaffold

@Composable
fun SavedPlacesScreen() {
    NearWakeScaffold(
        title = "Saved places",
        subtitle = "This list will mirror the Room-backed places table in a later pass.",
    ) {
        Text("Saved places placeholder")
    }
}
