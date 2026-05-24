package com.gosyria.app.ui.navigation

sealed class Screen(val route: String) {
    object Login      : Screen("login")
    object Role       : Screen("role")
    object RiderHome  : Screen("rider_home")
    object DriverHome : Screen("driver_home")
    object DriverTrip : Screen("driver_trip/{rideId}") {
        fun createRoute(rideId: String) = "driver_trip/$rideId"
    }
    object Tracking   : Screen("tracking/{rideId}") {
        fun createRoute(rideId: String) = "tracking/$rideId"
    }
}
