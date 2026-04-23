package com.nearwake.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.nearwake.core.designsystem.LocalSpacing
import com.nearwake.core.ui.HeroCard
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
) {
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
                text = "Travel calmer on the rides that are easiest to miss.",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            NearWakeStateChip(
                label = "Awaiting trip",
                state = NearWakeChipState.Neutral,
            )
            Text(
                text = "Choose a destination, set how early you want the alert, and let the app monitor in the background.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        HeroCard(modifier = Modifier.heightIn(min = spacing.massive * 2)) {
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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing.md),
        ) {
            OutlinedButton(onClick = onPermissions) { Text("Permissions") }
        }

        NearWakeSectionHeader(text = "Recent trips")
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
}
