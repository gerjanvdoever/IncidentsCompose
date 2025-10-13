package com.example.incidentscompose.navigation

sealed class Destinations(val route: String) {
    data object Login : Destinations("login")
    data object MyIncidentList : Destinations("myIncidentList")
    data object Register: Destinations("Register")
    data object UserProfile: Destinations("user_profile?userJson={userJson}") {
        fun createRoute(userJson: String): String {
            return "user_profile?userJson=$userJson"
        }
    }
    data object MyIncidentDetail : Destinations("myIncidentDetail")
    data object ReportIncident: Destinations("reportIncident")
    data object IncidentDetail : Destinations("incidentDetail")
    data object IncidentList : Destinations("incidentList")
    data object IncidentMap : Destinations("incidentMap")
    data object UserManagement : Destinations ("userManagement")
}