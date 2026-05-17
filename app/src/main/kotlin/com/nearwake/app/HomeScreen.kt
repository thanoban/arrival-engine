package com.nearwake.app

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DirectionsTransit
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
import com.nearwake.core.ui.NearWakeButtonSize
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
                activeTripStatus = state.statusLabel,
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
            size = NearWakeButtonSize.Medium,
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
    activeTripStatus: String,
    onSetDestination: () -> Unit,
    onOpenTrip: (String) -> Unit,
    delayMs: Int,
) {
    val spacing = LocalSpacing.current
    val colors = LocalNearWakeColors.current
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
    if (activeTrip != null) {
        ActiveTripCard(
            activeTrip = activeTrip,
            statusLabel = activeTripStatus,
            modifier = Modifier.alpha(alpha),
            onOpenTrip = onOpenTrip,
        )
    } else {
        HeroCard(
            modifier = Modifier
                .alpha(alpha)
                .heightIn(min = spacing.massive + spacing.xl),
        ) {
            Icon(
                imageVector = Icons.Filled.DirectionsTransit,
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .align(Alignment.CenterHorizontally),
                tint = colors.monitoringBase,
            )
            Text(
                text = "Where are you heading?",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            NearWakePrimaryButton(
                modifier = Modifier
                    .padding(top = spacing.md)
                    .fillMaxWidth(),
                text = "Search destination",
                onClick = onSetDestination,
                size = NearWakeButtonSize.Medium,
            )
        }
    }
}

@Composable
private fun ActiveTripCard(
    activeTrip: HomeActiveTrip,
    statusLabel: String,
    onOpenTrip: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    val colors = LocalNearWakeColors.current
    ElevatedCard(
        modifier = modifier
            .heightIn(min = 80.dp)
            .semantics {
                role = Role.Button
                contentDescription = "Resume live trip to ${activeTrip.destinationName}. Status $statusLabel."
            }
            .clickable { onOpenTrip(activeTrip.tripId) },
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.DirectionsTransit,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = colors.monitoringBase,
            )
            Spacer(modifier = Modifier.width(spacing.md))
            Text(
                text = activeTrip.destinationName,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium,
                color = colors.textPrimary,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            NearWakeStateChip(
                label = statusLabel,
                state = NearWakeChipState.Monitoring,
            )
            Spacer(modifier = Modifier.width(spacing.sm))
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = colors.textTertiary,
            )
        }
    }
}

@Composable
private fun RearmCard(
    destinationName: String,
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
    val colors = LocalNearWakeColors.current
    SurfaceCard(
        modifier = Modifier
            .alpha(alpha)
            .heightIn(min = 72.dp)
            .semantics {
                role = Role.Button
                contentDescription = "Re-arm trip to $destinationName."
            }
            .clickable(onClick = onRearm),
        compact = true,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.Replay,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = colors.approachBase,
            )
            Spacer(modifier = Modifier.width(spacing.md))
            Text(
                text = destinationName,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
                color = colors.textPrimary,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "Re-arm",
                style = MaterialTheme.typography.labelLarge,
                color = colors.safeBase,
            )
        }
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
        SurfaceCard(
            modifier = Modifier.semantics(mergeDescendants = true) {
                contentDescription =
                    "No trips yet. Your finished trips will appear here after you arm your first destination."
            },
        ) {
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
        Column {
            trips.forEachIndexed { index, trip ->
                RecentTripRow(
                    trip = trip,
                    staggerIndex = index,
                    showDivider = index != trips.lastIndex,
                )
            }
        }
    }
}

@Composable
private fun RecentTripRow(
    trip: HomeRecentTrip,
    staggerIndex: Int,
    showDivider: Boolean,
) {
    val spacing = LocalSpacing.current
    val colors = LocalNearWakeColors.current
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
    Column(modifier = Modifier.alpha(alpha)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .semantics(mergeDescendants = true) {
                    contentDescription = "${trip.destinationName}. ${trip.statusLabel}."
                },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = if (trip.statusTone == HomeStatusTone.Approaching) {
                    Icons.Filled.Warning
                } else {
                    Icons.Filled.CheckCircle
                },
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = trip.statusTone.iconColor(),
            )
            Spacer(modifier = Modifier.width(spacing.md))
            Text(
                text = trip.destinationName,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = trip.statusLabel,
                style = MaterialTheme.typography.labelSmall,
                color = colors.textTertiary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(start = spacing.xxxl),
                thickness = 1.dp,
                color = colors.borderSubtle,
            )
        }
    }
}

@Composable
private fun HomeStatusTone.iconColor() = when (this) {
    HomeStatusTone.Safe -> LocalNearWakeColors.current.safeBase
    HomeStatusTone.Monitoring -> LocalNearWakeColors.current.monitoringBase
    HomeStatusTone.Approaching -> LocalNearWakeColors.current.approachBase
    HomeStatusTone.Neutral -> LocalNearWakeColors.current.textTertiary
}

private fun HomeStatusTone.toChipState(): NearWakeChipState = when (this) {
    HomeStatusTone.Safe -> NearWakeChipState.Safe
    HomeStatusTone.Monitoring -> NearWakeChipState.Monitoring
    HomeStatusTone.Approaching -> NearWakeChipState.Approaching
    HomeStatusTone.Neutral -> NearWakeChipState.Neutral
}
