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
import com.nearwake.core.ui.NearWakeCard
import com.nearwake.core.ui.NearWakePrimaryButton
import com.nearwake.core.ui.NearWakeScaffold

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
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            state.pages.forEachIndexed { index, page ->
                NearWakeCard {
                    Text(
                        text = "0${index + 1}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = page,
                        style = MaterialTheme.typography.titleLarge,
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
