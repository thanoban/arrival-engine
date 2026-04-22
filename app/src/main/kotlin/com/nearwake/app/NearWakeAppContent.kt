package com.nearwake.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import com.nearwake.core.ui.NearWakeScaffold

@Composable
fun NearWakeAppContent(
    viewModel: NearWakeAppViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    val startDestination = state.startDestination
    if (startDestination == null) {
        NearWakeScaffold(
            title = "NearWake",
            subtitle = "Loading your arrival workspace.",
        ) {}
        return
    }

    NearWakeNavHost(startDestination = startDestination)
}
