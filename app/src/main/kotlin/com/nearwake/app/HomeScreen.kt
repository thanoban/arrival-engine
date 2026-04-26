package com.nearwake.app

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nearwake.core.designsystem.LocalNearWakeColors
import com.nearwake.core.designsystem.LocalSpacing
import com.nearwake.core.designsystem.NearWakeMotion
import com.nearwake.core.designsystem.ProvideNearWakeStateAccent
import com.nearwake.core.ui.ElevatedCard
import com.nearwake.core.ui.HeroCard
import com.nearwake.core.ui.NearWakeChipState
import com.nearwake.core.ui.NearWakePrimaryButton
import com.nearwake.core.ui.NearWakeScaffold
import com.nearwake.core.ui.NearWakeSectionHeader
import com.nearwake.core.ui.NearWakeStateChip
import com.nearwake.core.ui.NearWakeTextButton
import com.nearwake.core.ui.SurfaceCard
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    onSetDestination: () -> Unit,
    onHistory: () -> Unit,
    onSettings: () -> Unit,
    onPermissions: () -> Unit,
    onDepartureReminders: () -> Unit,
    onOpenTrip: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val spacing = LocalSpacing.current
    val themeColors = LocalNearWakeColors.current

    ProvideNearWakeStateAccent(themeColors.safeBase) {
        NearWakeScaffold(
            title = "NearWake",
            subtitle = null,
            topBarActions = {
                NearWakeTextButton(text = "History", onClick = onHistory)
                NearWakeTextButton(text = "Settings", onClick = onSettings)
            },
        ) {
            GreetingBlock(
                headline = state.headline,
                statusLabel = state.statusLabel,
                statusTone = state.statusTone,
                onPermissions = onPermissions,
            )

            DestinationHeroCard(
                activeTrip = state.activeTrip,
                onSetDestination = onSetDestination,
                onOpenTrip = onOpenTrip,
                delayMs = 60,
            )

            DepartureReminderCard(
                onDepartureReminders = onDepartureReminders,
                delayMs = 100,
            )

            state.rearmTrip?.let { rearmTrip ->
                RearmCard(
                    destinationName = rearmTrip.destinationName,
                    subtitle = rearmTrip.subtitle,
                    onRearm = { viewModel.rearmLastTrip(onOpenTrip) },
                    delayMs = 120,
                )
            }

            RecentTripsSection(
                trips = state.recentTrips,
                onViewAll = onHistory,
            )
        }
    }
}

@Composable
private fun DepartureReminderCard(
    onDepartureReminders: () -> Unit,
    delayMs: Int,
) {
    val spacing = LocalSpacing.current
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(delayMs.toLong())
        visible = true
    }
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = NearWakeMotion.Base),
        label = "departure-alpha",
    )
    SurfaceCard(modifier = Modifier.alpha(alpha)) {
        NearWakeSectionHeader(text = "Depart on time")
        Text(
            text = "See learned leave-by times from your trip history.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        NearWakePrimaryButton(
            modifier = Modifier.padding(top = spacing.md).fillMaxWidth(),
            text = "View leave-by times",
            onClick = onDepartureReminders,
        )
    }
}

@Composable
private fun GreetingBlock(
    headline: String,
    statusLabel: String,
    statusTone: HomeStatusTone,
    onPermissions: () -> Unit,
) {
    val spacing = LocalSpacing.current
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = NearWakeMotion.Base),
        label = "greeting-alpha",
    )
    val offsetY by animateFloatAsState(
        targetValue = if (visible) 0f else 8f,
        animationSpec = tween(durationMillis = NearWakeMotion.Base),
        label = "greeting-offset",
    )
    Column(
        modifier = Modifier
            .alpha(alpha)
            .offset(y = offsetY.dp),
        verticalArrangement = Arrangement.spacedBy(spacing.sm),
    ) {
        Text(
            text = headline,
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NearWakeStateChip(
                label = statusLabel,
                state = statusTone.toChipState(),
            )
            NearWakeTextButton(
                text = "Permissions",
                onClick = onPermissions,
            )
        }
        Text(
            text = if (statusTone == HomeStatusTone.Monitoring) {
                "You can jump back into the live view now, or set another destination once this trip is finished."
            } else {
                "Choose a destination, set how early you want the alert, and let the app monitor in the background."
            },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun DestinationHeroCard(
    activeTrip: HomeActiveTrip?,
    onSetDestination: () -> Unit,
    onOpenTrip: (String) -> Unit,
    delayMs: Int,
) {
    val spacing = LocalSpacing.current
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(delayMs.toLong())
        visible = true
    }
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = NearWakeMotion.Base),
        label = "hero-alpha",
    )
    HeroCard(
        modifier = Modifier
            .alpha(alpha)
            .heightIn(min = spacing.massive + spacing.xl),
    ) {
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
}

@Composable
private fun RearmCard(
    destinationName: String,
    subtitle: String,
    onRearm: () -> Unit,
    delayMs: Int,
) {
    val spacing = LocalSpacing.current
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(delayMs.toLong())
        visible = true
    }
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = NearWakeMotion.Base),
        label = "rearm-alpha",
    )
    ElevatedCard(modifier = Modifier.alpha(alpha)) {
        NearWakeSectionHeader(text = "One-tap re-arm")
        Text(
            text = destinationName,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        NearWakePrimaryButton(
            modifier = Modifier.padding(top = spacing.md).fillMaxWidth(),
            text = "Re-arm → $destinationName",
            onClick = onRearm,
        )
    }
}

@Composable
private fun RecentTripsSection(
    trips: List<HomeRecentTrip>,
    onViewAll: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NearWakeSectionHeader(text = "Recent trips")
        if (trips.isNotEmpty()) {
            NearWakeTextButton(text = "View all", onClick = onViewAll)
        }
    }
    if (trips.isEmpty()) {
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
    } else {
        trips.forEachIndexed { index, trip ->
            RecentTripRow(trip = trip, staggerIndex = index)
        }
    }
}

@Composable
private fun RecentTripRow(trip: HomeRecentTrip, staggerIndex: Int) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay((staggerIndex * 40).toLong())
        visible = true
    }
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = NearWakeMotion.Base),
        label = "trip-alpha",
    )
    SurfaceCard(modifier = Modifier.alpha(alpha)) {
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

private fun HomeStatusTone.toChipState(): NearWakeChipState = when (this) {
    HomeStatusTone.Safe -> NearWakeChipState.Safe
    HomeStatusTone.Monitoring -> NearWakeChipState.Monitoring
    HomeStatusTone.Approaching -> NearWakeChipState.Approaching
    HomeStatusTone.Neutral -> NearWakeChipState.Neutral
}
