package com.nearwake.app

sealed class NearWakeRoute(val route: String) {
    data object Onboarding : NearWakeRoute("onboarding")
    data object Home : NearWakeRoute("home")
    data object Permissions : NearWakeRoute("permissions")
    data object Places : NearWakeRoute("places")
    data object TripSetup : NearWakeRoute("trip_setup/{placeId}") {
        const val PLACE_ID_ARG = "placeId"

        fun createRoute(placeId: String): String = "trip_setup/$placeId"
    }
    data object LiveTrip : NearWakeRoute("live_trip/{tripId}") {
        const val TRIP_ID_ARG = "tripId"

        fun createRoute(tripId: String): String = "live_trip/$tripId"
    }
    data object Alert : NearWakeRoute("alert/{tripId}") {
        const val TRIP_ID_ARG = "tripId"

        fun createRoute(tripId: String): String = "alert/$tripId"
    }
    data object Recovery : NearWakeRoute("recovery/{tripId}") {
        const val TRIP_ID_ARG = "tripId"

        fun createRoute(tripId: String): String = "recovery/$tripId"
    }
    data object WalkFinish : NearWakeRoute("walk_finish/{tripId}") {
        const val TRIP_ID_ARG = "tripId"

        fun createRoute(tripId: String): String = "walk_finish/$tripId"
    }
    data object Companion : NearWakeRoute("companion/{tripId}") {
        const val TRIP_ID_ARG = "tripId"

        fun createRoute(tripId: String): String = "companion/$tripId"
    }
    data object History : NearWakeRoute("history")
    data object Departure : NearWakeRoute("departure")
    data object TripSummary : NearWakeRoute("trip_summary/{tripId}") {
        const val TRIP_ID_ARG = "tripId"

        fun createRoute(tripId: String): String = "trip_summary/$tripId"
    }
    data object Settings : NearWakeRoute("settings")
    data object Diagnostics : NearWakeRoute("diagnostics")
    data object SavedPlaces : NearWakeRoute("saved_places")
}
