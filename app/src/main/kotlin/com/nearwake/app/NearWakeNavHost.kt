package com.nearwake.app

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.nearwake.feature.alerts.AlertScreen
import com.nearwake.feature.alerts.RecoveryScreen
import com.nearwake.feature.diagnostics.DiagnosticsScreen
import com.nearwake.feature.history.HistoryScreen
import com.nearwake.feature.history.TripSummaryScreen
import com.nearwake.feature.livetrip.LiveTripScreen
import com.nearwake.feature.onboarding.OnboardingScreen
import com.nearwake.feature.permissions.PermissionsScreen
import com.nearwake.feature.places.PlaceSearchScreen
import com.nearwake.feature.settings.SettingsScreen
import com.nearwake.feature.tripsetup.TripSetupScreen
import com.nearwake.feature.walkfinish.WalkFinishScreen

@Composable
fun NearWakeNavHost(
    startDestination: String = NearWakeRoute.Onboarding.route,
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = startDestination,
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
                onPermissions = { navController.navigate(NearWakeRoute.Permissions.route) },
                onOpenTrip = { tripId ->
                    navController.navigate(NearWakeRoute.LiveTrip.createRoute(tripId))
                },
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
                onSelectPlace = { placeId ->
                    navController.navigate(NearWakeRoute.TripSetup.createRoute(placeId))
                },
                onBack = { navController.popBackStack() },
            )
        }
        composable(NearWakeRoute.TripSetup.route) {
            TripSetupScreen(
                onStartTrip = { tripId ->
                    navController.navigate(NearWakeRoute.LiveTrip.createRoute(tripId))
                },
                onBack = { navController.popBackStack() },
            )
        }
        composable(NearWakeRoute.LiveTrip.route) {
            LiveTripScreen(
                onCancel = { navController.popBackStack(NearWakeRoute.Home.route, false) },
                onSimulateAlert = { tripId ->
                    navController.navigate(NearWakeRoute.Alert.createRoute(tripId))
                },
            )
        }
        composable(NearWakeRoute.Alert.route) {
            AlertScreen(
                onDismiss = { tripId ->
                    navController.navigate(NearWakeRoute.WalkFinish.createRoute(tripId))
                },
                onRecovery = { tripId ->
                    navController.navigate(NearWakeRoute.Recovery.createRoute(tripId))
                },
            )
        }
        composable(NearWakeRoute.Recovery.route) {
            RecoveryScreen(
                onEndTrip = { navController.navigate(NearWakeRoute.Home.route) },
                onResumeMonitoring = { tripId ->
                    navController.navigate(NearWakeRoute.LiveTrip.createRoute(tripId))
                },
            )
        }
        composable(NearWakeRoute.History.route) {
            HistoryScreen(
                onBack = { navController.popBackStack() },
                onTripSelected = { tripId ->
                    navController.navigate(NearWakeRoute.TripSummary.createRoute(tripId))
                },
            )
        }
        composable(NearWakeRoute.TripSummary.route) {
            TripSummaryScreen(
                onBack = { navController.popBackStack() },
            )
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
        composable(NearWakeRoute.WalkFinish.route) {
            WalkFinishScreen(
                onArrived = { navController.navigate(NearWakeRoute.Home.route) },
            )
        }
    }
}
