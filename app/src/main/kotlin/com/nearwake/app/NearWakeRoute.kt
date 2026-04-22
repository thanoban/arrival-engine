package com.nearwake.app

sealed class NearWakeRoute(val route: String) {
    data object Onboarding : NearWakeRoute("onboarding")
    data object Home : NearWakeRoute("home")
    data object TripSetup : NearWakeRoute("trip_setup")
    data object LiveTrip : NearWakeRoute("live_trip")
    data object Alert : NearWakeRoute("alert")
    data object Recovery : NearWakeRoute("recovery")
    data object History : NearWakeRoute("history")
    data object Settings : NearWakeRoute("settings")
}
