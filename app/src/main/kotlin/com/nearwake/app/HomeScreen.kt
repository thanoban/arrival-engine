package com.nearwake.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nearwake.core.ui.NearWakeCard
import com.nearwake.core.ui.NearWakePrimaryButton
import com.nearwake.core.ui.NearWakeScaffold

@Composable
fun HomeScreen(
    onSetDestination: () -> Unit,
    onHistory: () -> Unit,
    onSettings: () -> Unit,
) {
    NearWakeScaffold(
        title = "NearWake",
        subtitle = "Arrival assurance for sleepy rides, missed transfers, and low-battery commutes.",
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedButton(onClick = onSettings) { Text("Settings") }
            OutlinedButton(onClick = onHistory) { Text("History") }
        }

        NearWakePrimaryButton(
            text = "+ Set destination",
            onClick = onSetDestination,
        )

        NearWakeCard {
            Text(
                text = "Recent places",
                style = MaterialTheme.typography.titleLarge,
            )
            Text("Central Station")
            Text("Airport Terminal 2")
            Text("Last trip: Completed 2h ago", color = MaterialTheme.colorScheme.primary)
        }
    }
}
