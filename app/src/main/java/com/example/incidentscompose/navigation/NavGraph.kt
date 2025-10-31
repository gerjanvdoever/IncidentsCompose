package com.example.incidentscompose.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.incidentscompose.ui.screens.auth.LoginScreen
import com.example.incidentscompose.ui.screens.auth.RegisterScreen
import com.example.incidentscompose.ui.screens.auth.UserProfileScreen
import com.example.incidentscompose.ui.screens.incidents.MyIncidentListScreen
import com.example.incidentscompose.ui.screens.incidents.ReportIncidentScreen
import com.example.incidentscompose.ui.screens.incidents.MyIncidentDetailScreen
import com.example.incidentscompose.ui.screens.management.IncidentDetailScreen
import com.example.incidentscompose.ui.screens.management.IncidentListScreen
import com.example.incidentscompose.ui.screens.management.IncidentMapScreen
import com.example.incidentscompose.ui.screens.management.UserManagementScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Destinations.Login.route
    ) {
        composable(Destinations.Login.route) {
            LoginScreen(navController)
        }
        composable(Destinations.MyIncidentList.route) {
            MyIncidentListScreen(navController)
        }
        composable(Destinations.Register.route) {
            RegisterScreen(navController)
        }
        composable(
            route = Destinations.UserProfile.route,
            arguments = emptyList()
        ) { backStackEntry ->
            val userJson = backStackEntry.arguments?.getString("userJson")
            UserProfileScreen(
                navController = navController,
                userJson = userJson
            )
        }
        composable(Destinations.MyIncidentDetail.route) {
            MyIncidentDetailScreen(navController)
        }
        composable(Destinations.ReportIncident.route) {
            ReportIncidentScreen(navController)
        }
        composable(Destinations.IncidentDetail.route) { backStackEntry ->
            val incidentId = backStackEntry.arguments?.getString("incidentId")?.toLongOrNull()
            IncidentDetailScreen(navController = navController, incidentId = incidentId)
        }
        composable(Destinations.IncidentList.route) {
            IncidentListScreen(navController)
        }
        composable(Destinations.IncidentMap.route) {
            IncidentMapScreen(navController)
        }
        composable(Destinations.UserManagement.route) {
            UserManagementScreen(navController)
        }
    }
}