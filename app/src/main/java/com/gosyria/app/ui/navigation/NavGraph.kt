package com.gosyria.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.gosyria.app.data.model.UserRole
import com.gosyria.app.ui.AppViewModel
import com.gosyria.app.ui.screens.auth.LoginScreen
import com.gosyria.app.ui.screens.driver.DriverHomeScreen
import com.gosyria.app.ui.screens.driver.DriverTripScreen
import com.gosyria.app.ui.screens.rider.RiderHomeScreen
import com.gosyria.app.ui.screens.rider.TrackingScreen
import com.gosyria.app.ui.screens.role.RoleSelectionScreen

@Composable
fun NavGraph(appViewModel: AppViewModel = hiltViewModel()) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = appViewModel.startRoute) {

        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = { role ->
                    val dest = if (role == UserRole.DRIVER) Screen.DriverHome.route else Screen.RiderHome.route
                    navController.navigate(dest) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
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
                onLogout = {
                    appViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.RiderHome.route) {
            RiderHomeScreen(
                onRideStarted = { rideId ->
                    navController.navigate(Screen.Tracking.createRoute(rideId))
                },
                onLogout = {
                    navController.navigate(Screen.Role.route) {
                        popUpTo(Screen.RiderHome.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.DriverHome.route) {
            DriverHomeScreen(
                onLogout = {
                    navController.navigate(Screen.Role.route) {
                        popUpTo(Screen.DriverHome.route) { inclusive = true }
                    }
                },
                onRideAccepted = { rideId ->
                    navController.navigate(Screen.DriverTrip.createRoute(rideId))
                }
            )
        }

        composable(
            route = Screen.DriverTrip.route,
            arguments = listOf(navArgument("rideId") { type = NavType.StringType }),
        ) {
            DriverTripScreen(
                onTripCompleted = {
                    navController.navigate(Screen.DriverHome.route) {
                        popUpTo(Screen.DriverHome.route) { inclusive = true }
                    }
                }
            )
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
