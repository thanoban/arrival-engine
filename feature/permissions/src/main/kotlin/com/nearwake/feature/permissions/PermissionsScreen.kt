package com.nearwake.feature.permissions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nearwake.core.designsystem.LocalSpacing
import com.nearwake.core.designsystem.NearWakeColors
import com.nearwake.core.designsystem.ProvideNearWakeStateAccent
import com.nearwake.core.ui.NearWakeChipState
import com.nearwake.core.ui.NearWakePrimaryButton
import com.nearwake.core.ui.NearWakeScaffold
import com.nearwake.core.ui.NearWakeSecondaryButton
import com.nearwake.core.ui.NearWakeStateChip
import com.nearwake.core.ui.NearWakeTextButton
import com.nearwake.core.ui.SurfaceCard

@Composable
fun PermissionsScreen(
    onGrantLocation: () -> Unit,
    onUseLimitedMode: () -> Unit,
    onBack: () -> Unit,
    viewModel: PermissionsViewModel = viewModel(),
) {
    val state = viewModel.state.value
    val spacing = LocalSpacing.current

    ProvideNearWakeStateAccent(NearWakeColors.SafeBase) {
        NearWakeScaffold(
            title = "Permissions",
            subtitle = null,
            topBarActions = {
                NearWakeTextButton(text = "Back", onClick = onBack)
            },
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.md)) {
                PermissionStatusCard(
                    title = "Notifications",
                    body = "NearWake can't wake you near your stop unless Android is allowed to show alarms.",
                    granted = state.notificationGranted,
                )
                PermissionStatusCard(
                    title = "Precise location",
                    body = "Location lets NearWake know where your trip is starting and where your destination is.",
                    granted = state.fineLocationGranted,
                )
                PermissionStatusCard(
                    title = "Background location",
                    body = "To alert you while your screen is off or you're asleep, NearWake needs background location during active trips only.",
                    granted = state.backgroundLocationGranted,
                )
                PermissionStatusCard(
                    title = "Activity recognition",
                    body = "Helps the engine scale power use up and down more intelligently based on whether you're moving.",
                    granted = state.activityRecognitionGranted,
                )
            }

            NearWakePrimaryButton(
                modifier = Modifier.fillMaxWidth(),
                text = "Grant location",
                onClick = onGrantLocation,
            )
            NearWakeSecondaryButton(
                modifier = Modifier.fillMaxWidth(),
                text = "Use limited mode",
                onClick = onUseLimitedMode,
            )
        }
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
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
