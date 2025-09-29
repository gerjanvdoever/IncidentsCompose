package com.example.incidentscompose.navigation

sealed class Destinations(val route: String) {
    data object Login : Destinations("login")
    data object MyIncidentList : Destinations("myIncidentList")
    data object Register: Destinations("Register")
}