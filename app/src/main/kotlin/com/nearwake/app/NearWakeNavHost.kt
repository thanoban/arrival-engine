package com.nearwake.app

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.nearwake.feature.alerts.AlertScreen
import com.nearwake.feature.alerts.RecoveryScreen
import com.nearwake.feature.diagnostics.DiagnosticsScreen
import com.nearwake.feature.history.HistoryScreen
import com.nearwake.feature.livetrip.LiveTripScreen
import com.nearwake.feature.onboarding.OnboardingScreen
import com.nearwake.feature.permissions.PermissionsScreen
import com.nearwake.feature.places.PlaceSearchScreen
import com.nearwake.feature.settings.SettingsScreen
import com.nearwake.feature.tripsetup.TripSetupScreen

@Composable
fun NearWakeNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = NearWakeRoute.Onboarding.route,
    ) {
        composable(NearWakeRoute.Onboarding.route) {
            OnboardingScreen(
                onContinue = { navController.navigate(NearWakeRoute.Home.route) },
            )
        }
        composable(NearWakeRoute.Home.route) {
            HomeScreen(
                onSetDestination = { navController.navigate(NearWakeRoute.Places.route) },
                onHistory = { navController.navigate(NearWakeRoute.History.route) },
                onSettings = { navController.navigate(NearWakeRoute.Settings.route) },
            )
        }
        composable(NearWakeRoute.Permissions.route) {
            PermissionsScreen(
                onGrantLocation = { navController.popBackStack() },
                onUseLimitedMode = { navController.popBackStack() },
                onBack = { navController.popBackStack() },
            )
        }
        composable(NearWakeRoute.Places.route) {
            PlaceSearchScreen(
                onSelectPlace = { navController.navigate(NearWakeRoute.TripSetup.route) },
                onBack = { navController.popBackStack() },
            )
        }
        composable(NearWakeRoute.TripSetup.route) {
            TripSetupScreen(
                onStartTrip = { navController.navigate(NearWakeRoute.LiveTrip.route) },
                onBack = { navController.popBackStack() },
            )
        }
        composable(NearWakeRoute.LiveTrip.route) {
            LiveTripScreen(
                onCancel = { navController.popBackStack(NearWakeRoute.Home.route, false) },
                onSimulateAlert = { navController.navigate(NearWakeRoute.Alert.route) },
            )
        }
        composable(NearWakeRoute.Alert.route) {
            AlertScreen(
                onDismiss = { navController.navigate(NearWakeRoute.Home.route) },
                onRecovery = { navController.navigate(NearWakeRoute.Recovery.route) },
            )
        }
        composable(NearWakeRoute.Recovery.route) {
            RecoveryScreen(
                onEndTrip = { navController.navigate(NearWakeRoute.Home.route) },
            )
        }
        composable(NearWakeRoute.History.route) {
            HistoryScreen(onBack = { navController.popBackStack() })
        }
        composable(NearWakeRoute.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onDiagnostics = { navController.navigate(NearWakeRoute.Diagnostics.route) },
            )
        }
        composable(NearWakeRoute.Diagnostics.route) {
            DiagnosticsScreen(
                onBack = { navController.popBackStack() },
            )
        }
    }
}
