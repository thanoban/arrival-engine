package com.nearwake.feature.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nearwake.core.ui.HeroCard
import com.nearwake.core.ui.NearWakeChipState
import com.nearwake.core.ui.NearWakePrimaryButton
import com.nearwake.core.ui.NearWakeScaffold
import com.nearwake.core.ui.NearWakeStateChip
import com.nearwake.core.ui.SurfaceCard

@Composable
fun OnboardingScreen(
    onContinue: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    NearWakeScaffold(
        title = "Wake before your stop",
        subtitle = "NearWake arms in seconds and stays calm until it really matters.",
    ) {
        HeroCard {
            NearWakeStateChip(
                label = "Privacy-first",
                state = NearWakeChipState.Safe,
            )
            Text(
                text = "Background monitoring, quiet until needed, and built for the commutes where fatigue makes timing hard.",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            state.pages.forEachIndexed { index, page ->
                SurfaceCard {
                    Text(
                        text = "0${index + 1}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = page,
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Text(
                        text = "Step ${index + 1} of ${state.pages.size}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        NearWakePrimaryButton(
            text = "Start setup",
            onClick = { viewModel.completeOnboarding(onContinue) },
        )
    }
}
