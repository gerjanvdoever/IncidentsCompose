package com.example.incidentscompose.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.incidentscompose.ui.screens.auth.LoginScreen
import com.example.incidentscompose.ui.screens.auth.RegisterScreen
import com.example.incidentscompose.ui.screens.incidents.MyIncidentListScreen
import com.example.incidentscompose.ui.screens.incidents.ReportIncidentScreen
import com.example.incidentscompose.ui.screens.incidents.MyIncidentDetailScreen
import com.example.incidentscompose.ui.screens.management.IncidentListScreen

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
        composable(Destinations.UserProfile.route) {
            TODO("Implement UserProfileScreen composable")
        }
        composable(Destinations.MyIncidentDetail.route) {
            MyIncidentDetailScreen(navController)
        }
        composable(Destinations.ReportIncident.route) {
            ReportIncidentScreen(navController)
        }
        composable(Destinations.IncidentDetail.route) {
            TODO("Implement IncidentDetailScreen composable")
        }
        composable(Destinations.IncidentList.route) {
            IncidentListScreen(navController)
        }
        composable(Destinations.IncidentMap.route) {
            TODO("Implement IncidentMapScreen composable")
        }
        composable(Destinations.UserManagement.route) {
            TODO("Implement UserManagementScreen composable")
        }
    }
}