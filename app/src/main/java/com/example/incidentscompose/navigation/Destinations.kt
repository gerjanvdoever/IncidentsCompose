package com.example.incidentscompose.navigation

sealed class Destinations(val route: String) {
    data object Login : Destinations("login")
}