package com.nearwake.feature.permissions

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.nearwake.core.ui.NearWakeCard
import com.nearwake.core.ui.NearWakeScaffold

@Composable
fun PermissionsHelpSheet() {
    NearWakeScaffold(
        title = "Why these permissions?",
        subtitle = "Each permission maps to one job in the arrival engine, and the app degrades safely when one is missing.",
    ) {
        NearWakeCard {
            Text("Location", style = MaterialTheme.typography.titleMedium)
            Text("Needed to register destination geofences and confirm when you are near your stop.")
        }
        NearWakeCard {
            Text("Activity recognition", style = MaterialTheme.typography.titleMedium)
            Text("Lets monitoring stay quiet until the phone believes you actually started moving.")
        }
        NearWakeCard {
            Text("Notifications", style = MaterialTheme.typography.titleMedium)
            Text("Makes sure the arrival alarm can break through when the screen is off or you are asleep.")
        }
        NearWakeCard {
            Text("If you decline one", style = MaterialTheme.typography.titleMedium)
            Text("NearWake falls back to foreground-only behavior instead of blocking the trip flow.")
        }
    }
}
