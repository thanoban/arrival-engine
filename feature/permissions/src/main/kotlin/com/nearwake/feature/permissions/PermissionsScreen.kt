package com.nearwake.feature.permissions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nearwake.core.ui.NearWakeChipState
import com.nearwake.core.ui.NearWakePrimaryButton
import com.nearwake.core.ui.NearWakeScaffold
import com.nearwake.core.ui.NearWakeSecondaryButton
import com.nearwake.core.ui.NearWakeStateChip
import com.nearwake.core.ui.SurfaceCard

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
        topBarActions = {
            NearWakeSecondaryButton(text = "Back", onClick = onBack)
        },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            PermissionStatusCard(
                title = "Notifications",
                body = "Required so the arrival alert can actually get your attention.",
                granted = state.notificationGranted,
            )
            PermissionStatusCard(
                title = "Precise location",
                body = "Improves route awareness and destination confidence.",
                granted = state.fineLocationGranted,
            )
            PermissionStatusCard(
                title = "Background location",
                body = "Lets monitoring continue when you put the phone away.",
                granted = state.backgroundLocationGranted,
            )
            PermissionStatusCard(
                title = "Activity recognition",
                body = "Helps the engine scale power use up and down more intelligently.",
                granted = state.activityRecognitionGranted,
            )
        }
        NearWakePrimaryButton(
            text = "Grant location",
            onClick = onGrantLocation,
        )
        NearWakeSecondaryButton(text = "Use limited mode", onClick = onUseLimitedMode)
    }
}

@Composable
private fun PermissionStatusCard(
    title: String,
    body: String,
    granted: Boolean,
) {
    SurfaceCard {
        NearWakeStateChip(
            label = if (granted) "Granted" else "Needed",
            state = if (granted) NearWakeChipState.Safe else NearWakeChipState.Approaching,
        )
        Text(text = title, style = MaterialTheme.typography.titleLarge)
        Text(body, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
