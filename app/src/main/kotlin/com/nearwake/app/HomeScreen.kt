package com.nearwake.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nearwake.core.designsystem.LocalSpacing
import com.nearwake.core.ui.HeroCard
import com.nearwake.core.ui.NearWakeSecondaryButton
import com.nearwake.core.ui.NearWakePrimaryButton
import com.nearwake.core.ui.NearWakeScaffold
import com.nearwake.core.ui.NearWakeSectionHeader
import com.nearwake.core.ui.NearWakeStateChip
import com.nearwake.core.ui.NearWakeChipState
import com.nearwake.core.ui.SurfaceCard
import com.nearwake.core.ui.NearWakeTextButton

@Composable
fun HomeScreen(
    onSetDestination: () -> Unit,
    onHistory: () -> Unit,
    onSettings: () -> Unit,
    onPermissions: () -> Unit,
    onOpenTrip: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val spacing = LocalSpacing.current
    NearWakeScaffold(
        title = "NearWake",
        subtitle = null,
        topBarActions = {
            NearWakeTextButton(text = "History", onClick = onHistory)
            NearWakeTextButton(text = "Settings", onClick = onSettings)
        },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(spacing.md)) {
            Text(
                text = state.headline,
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            NearWakeStateChip(
                label = state.statusLabel,
                state = state.statusTone.toChipState(),
            )
            Text(
                text = if (state.activeTrip != null) {
                    "You can jump back into the live view now, or set another destination once this trip is finished."
                } else {
                    "Choose a destination, set how early you want the alert, and let the app monitor in the background."
                },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        HeroCard(modifier = Modifier.heightIn(min = spacing.massive * 2)) {
            val activeTrip = state.activeTrip
            if (activeTrip != null) {
                Text(
                    text = activeTrip.destinationName,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = activeTrip.subtitle,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                NearWakePrimaryButton(
                    modifier = Modifier.padding(top = spacing.md),
                    text = "Resume live trip",
                    onClick = { onOpenTrip(activeTrip.tripId) },
                )
            } else {
                Text(
                    text = "Set your next stop",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = "NearWake will preview the route when it can, then keep destination-only monitoring as a safe fallback.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                NearWakePrimaryButton(
                    modifier = Modifier.padding(top = spacing.md),
                    text = "Choose destination",
                    onClick = onSetDestination,
                )
            }
        }

        state.rearmTrip?.let { rearmTrip ->
            SurfaceCard {
                NearWakeSectionHeader(text = "One-tap re-arm")
                Text(
                    text = rearmTrip.destinationName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = rearmTrip.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                NearWakePrimaryButton(
                    modifier = Modifier.padding(top = spacing.md),
                    text = "Re-arm last trip",
                    onClick = { viewModel.rearmLastTrip(onOpenTrip) },
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing.md),
        ) {
            NearWakeSecondaryButton(text = "Permissions", onClick = onPermissions)
        }

        NearWakeSectionHeader(text = "Recent trips")
        if (state.recentTrips.isEmpty()) {
            SurfaceCard {
                Text(
                    text = "No trips yet",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = "Your finished trips will appear here after you arm your first destination.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        state.recentTrips.forEach { trip ->
            SurfaceCard {
                NearWakeStateChip(
                    label = trip.statusLabel,
                    state = trip.statusTone.toChipState(),
                )
                Text(
                    text = trip.destinationName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = trip.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun HomeStatusTone.toChipState(): NearWakeChipState = when (this) {
    HomeStatusTone.Safe -> NearWakeChipState.Safe
    HomeStatusTone.Monitoring -> NearWakeChipState.Monitoring
    HomeStatusTone.Approaching -> NearWakeChipState.Approaching
    HomeStatusTone.Neutral -> NearWakeChipState.Neutral
}
