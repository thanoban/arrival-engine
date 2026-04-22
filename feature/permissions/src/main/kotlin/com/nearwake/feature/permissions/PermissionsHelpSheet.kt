package com.nearwake.feature.permissions

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.nearwake.core.ui.NearWakeScaffold

@Composable
fun PermissionsHelpSheet() {
    NearWakeScaffold(
        title = "Why these permissions?",
        subtitle = "Location tells us where you are, activity tells us when you started moving, and notifications make sure the alert can break through sleep.",
    ) {
        Text("Permissions help content placeholder")
    }
}
