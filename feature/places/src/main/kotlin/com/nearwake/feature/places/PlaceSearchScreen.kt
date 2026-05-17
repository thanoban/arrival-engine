package com.nearwake.feature.places

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nearwake.core.designsystem.LocalRadius
import com.nearwake.core.designsystem.NearWakeColors
import com.nearwake.core.designsystem.ProvideNearWakeStateAccent
import com.nearwake.core.ui.NearWakeScaffold
import com.nearwake.core.ui.NearWakeSectionHeader
import com.nearwake.core.ui.NearWakeTextButton
import com.nearwake.core.ui.PlaceResultKind
import com.nearwake.core.ui.PlaceResultRow
import com.nearwake.core.ui.SurfaceCard

@Composable
fun PlaceSearchScreen(
    onSelectPlace: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: PlaceSearchViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val radius = LocalRadius.current

    ProvideNearWakeStateAccent(NearWakeColors.BrandBase) {
        NearWakeScaffold(
            title = "Pick a destination",
            subtitle = null,
            topBarActions = {
                NearWakeTextButton(text = "Back", onClick = onBack)
            },
        ) {
            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::updateQuery,
                label = { Text("Search destination") },
                placeholder = { Text("Search stations, airports, landmarks…") },
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "Search destination. Type a station, airport, or landmark name." },
                singleLine = true,
                shape = RoundedCornerShape(radius.sm),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NearWakeColors.BrandBase,
                    unfocusedBorderColor = NearWakeColors.BorderDefault,
                    focusedContainerColor = NearWakeColors.BgElevated,
                    unfocusedContainerColor = NearWakeColors.BgElevated,
                    focusedTextColor = NearWakeColors.TextPrimary,
                    unfocusedTextColor = NearWakeColors.TextPrimary,
                    cursorColor = NearWakeColors.BrandBase,
                ),
            )

            if (state.savedPlaces.isNotEmpty()) {
                NearWakeSectionHeader(text = "Saved places")
                SavedPlacesSection(
                    places = state.savedPlaces,
                    onSelectPlace = { placeId -> viewModel.selectSavedPlace(placeId, onSelectPlace) },
                )
            }

            if (state.query.length >= 2) {
                NearWakeSectionHeader(text = "Search results")
                state.errorMessage?.let { message ->
                    SurfaceCard(
                        modifier = Modifier.semantics {
                            contentDescription = "Search error. $message"
                        },
                        compact = true,
                    ) {
                        Text(
                            text = message,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                }
                if (state.isSearching) {
                    SurfaceCard(
                        modifier = Modifier.semantics {
                            contentDescription = "Searching places"
                        },
                        compact = true,
                    ) {
                        Text(
                            text = "Searching places...",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                } else if (state.results.isEmpty() && state.errorMessage == null) {
                    SurfaceCard(
                        modifier = Modifier.semantics {
                            contentDescription =
                                "No places matched that search yet. Try a broader station, airport, or landmark name."
                        },
                        compact = true,
                    ) {
                        Text(
                            text = "No places matched that search yet.",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        Text(
                            text = "Try a broader station, airport, or landmark name.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    Column {
                        state.results.forEachIndexed { index, result ->
                            PlaceResultRow(
                                name = result.name,
                                address = result.address,
                                kind = PlaceResultKind.Result,
                                showDivider = index != state.results.lastIndex,
                                onClick = { viewModel.selectResult(result, onSelectPlace) },
                            )
                        }
                    }
                }
            } else if (state.savedPlaces.isEmpty()) {
                SurfaceCard(
                    modifier = Modifier.semantics {
                        contentDescription =
                            "Start typing to search places. Saved places will also appear here for quick reuse."
                    },
                    compact = true,
                ) {
                    Text(
                        text = "Start typing to search places",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Text(
                        text = "Your saved places will also appear here for quick re-use.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
