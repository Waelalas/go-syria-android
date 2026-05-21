package com.gosyria.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.gosyria.app.ui.screens.auth.LoginScreen
import com.gosyria.app.ui.screens.driver.DriverHomeScreen
import com.gosyria.app.ui.screens.rider.RiderHomeScreen
import com.gosyria.app.ui.screens.rider.TrackingScreen
import com.gosyria.app.ui.screens.role.RoleSelectionScreen

@Composable
fun NavGraph() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.Login.route) {

        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = { navController.navigate(Screen.Role.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }}
            )
        }

        composable(Screen.Role.route) {
            RoleSelectionScreen(
                onRider  = { navController.navigate(Screen.RiderHome.route) {
                    popUpTo(Screen.Role.route) { inclusive = true }
                }},
                onDriver = { navController.navigate(Screen.DriverHome.route) {
                    popUpTo(Screen.Role.route) { inclusive = true }
                }},
            )
        }

        composable(Screen.RiderHome.route) {
            RiderHomeScreen(
                onRideStarted = { rideId ->
                    navController.navigate(Screen.Tracking.createRoute(rideId))
                }
            )
        }

        composable(Screen.DriverHome.route) {
            DriverHomeScreen()
        }

        composable(
            route = Screen.Tracking.route,
            arguments = listOf(navArgument("rideId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val rideId = backStackEntry.arguments?.getString("rideId") ?: return@composable
            TrackingScreen(
                rideId = rideId,
                onRideCompleted = {
                    navController.navigate(Screen.RiderHome.route) {
                        popUpTo(Screen.RiderHome.route) { inclusive = true }
                    }
                },
            )
        }
    }
}
