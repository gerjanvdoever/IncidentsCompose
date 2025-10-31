package com.example.incidentscompose.navigation

sealed class Destinations(val route: String) {
    data object Login : Destinations("login")
    data object MyIncidentList : Destinations("myIncident_list")
    data object Register: Destinations("register")
    data object UserProfile: Destinations("user_profile?userJson={userJson}") {
        fun createRoute(userJson: String): String {
            return "user_profile?userJson=$userJson"
        }
    }
    data object MyIncidentDetail : Destinations("my_incident_detail")
    data object ReportIncident: Destinations("report_incident")
    data object IncidentDetail : Destinations("incident_detail/{incidentId}") {
        fun createRoute(incidentId: Long): String {
            return "incident_detail/$incidentId"
        }
    }
    data object IncidentList : Destinations("incident_list")
    data object IncidentMap : Destinations("incident_map")
    data object UserManagement : Destinations ("user_management")
}