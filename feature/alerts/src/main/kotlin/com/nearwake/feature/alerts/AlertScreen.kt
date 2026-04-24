package com.nearwake.feature.alerts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nearwake.core.designsystem.LocalSpacing
import com.nearwake.core.designsystem.NearWakeColors
import com.nearwake.core.ui.NearWakeChipState
import com.nearwake.core.ui.NearWakeNumericText
import com.nearwake.core.ui.NearWakeStateChip
import com.nearwake.core.ui.PulseRing

@Composable
fun AlertScreen(
    onDismiss: () -> Unit,
    onRecovery: (String) -> Unit,
    viewModel: AlertViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val spacing = LocalSpacing.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NearWakeColors.AlertIntense)
            .padding(horizontal = spacing.xl, vertical = spacing.massive),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(spacing.sm),
            ) {
                NearWakeStateChip(
                    label = "Stage C - Arrival",
                    state = NearWakeChipState.Alert,
                )
                Text(
                    text = "Exit now",
                    style = MaterialTheme.typography.titleLarge,
                    color = NearWakeColors.TextPrimary.copy(alpha = 0.85f),
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(spacing.md),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    PulseRing(
                        color = NearWakeColors.TextPrimary.copy(alpha = 0.35f),
                        diameter = spacing.massive * 4,
                    )
                    Spacer(modifier = Modifier.size(spacing.massive * 3))
                }
                Text(
                    text = "ARRIVING",
                    style = MaterialTheme.typography.displayLarge,
                    color = NearWakeColors.TextPrimary,
                )
                Text(
                    text = state.destinationName,
                    style = MaterialTheme.typography.headlineLarge,
                    color = NearWakeColors.TextPrimary,
                )
                NearWakeNumericText(
                    text = state.etaLabel.filter { it.isDigit() }.ifBlank { "0" },
                    color = NearWakeColors.TextPrimary,
                    style = MaterialTheme.typography.displayMedium,
                )
                Text(
                    text = if (state.etaLabel.any { it.isDigit() }) "minutes remaining" else "arriving now",
                    style = MaterialTheme.typography.titleLarge,
                    color = NearWakeColors.TextPrimary.copy(alpha = 0.85f),
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(spacing.lg),
            ) {
                Button(
                    onClick = { viewModel.dismissTrip(onDismiss) },
                    modifier = Modifier.size(spacing.massive * 2 + spacing.xl),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NearWakeColors.TextPrimary,
                        contentColor = NearWakeColors.AlertIntense,
                    ),
                ) {
                    Text(
                        text = "Dismiss",
                        style = MaterialTheme.typography.titleLarge,
                    )
                }
                TextButton(
                    onClick = { onRecovery(state.tripId) },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = NearWakeColors.TextPrimary.copy(alpha = 0.8f),
                    ),
                ) {
                    Text(
                        text = "Need recovery?",
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }
}
