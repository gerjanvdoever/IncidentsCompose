package com.example.incidentscompose.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.incidentscompose.ui.screens.auth.LoginScreen

sealed class Screen(val route: String) {
    data object Login : Screen("login")
}

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                isBusy = false,
                validationError = null
            )
        }
    }
}
