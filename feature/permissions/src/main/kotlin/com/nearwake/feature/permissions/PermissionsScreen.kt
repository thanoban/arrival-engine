package com.nearwake.feature.permissions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nearwake.core.ui.NearWakeCard
import com.nearwake.core.ui.NearWakePrimaryButton
import com.nearwake.core.ui.NearWakeScaffold

@Composable
fun PermissionsScreen(
    onGrantLocation: () -> Unit,
    onUseLimitedMode: () -> Unit,
    onBack: () -> Unit,
    viewModel: PermissionsViewModel = viewModel(),
) {
    val state = viewModel.state.value
    NearWakeScaffold(
        title = "Permissions help",
        subtitle = "NearWake degrades gracefully, but a few permissions make the monitoring engine much more reliable.",
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            PermissionStatusCard("Notifications", state.notificationGranted)
            PermissionStatusCard("Precise location", state.fineLocationGranted)
            PermissionStatusCard("Background location", state.backgroundLocationGranted)
            PermissionStatusCard("Activity recognition", state.activityRecognitionGranted)
        }
        NearWakePrimaryButton(
            text = "Grant location",
            onClick = onGrantLocation,
        )
        OutlinedButton(onClick = onUseLimitedMode) { Text("Use limited mode") }
        OutlinedButton(onClick = onBack) { Text("Back") }
    }
}

@Composable
private fun PermissionStatusCard(
    title: String,
    granted: Boolean,
) {
    NearWakeCard {
        Text(text = title, style = MaterialTheme.typography.titleLarge)
        Text(if (granted) "Granted" else "Not granted")
    }
}
