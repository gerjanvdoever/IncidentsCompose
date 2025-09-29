package com.example.incidentscompose.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.incidentscompose.ui.screens.auth.LoginScreen
import com.example.incidentscompose.ui.screens.incidents.MyIncidentListScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Destinations.Login.route
    ) {
        composable(Destinations.Login.route) {
            LoginScreen(navController
            )
        }
        composable(Destinations.MyIncidentList.route) {
            MyIncidentListScreen(navController)
        }
    }
}