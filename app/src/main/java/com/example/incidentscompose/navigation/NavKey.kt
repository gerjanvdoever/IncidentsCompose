package com.example.incidentscompose.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
object LoginKey : NavKey

object MyIncidentListKey : NavKey
object RegisterKey : NavKey
object MyIncidentDetailKey : NavKey
object ReportIncidentKey : NavKey
object IncidentListKey : NavKey
object IncidentMapKey : NavKey
object UserManagementKey : NavKey

@Serializable
data class UserProfileKey(val userJson: String) : NavKey

@Serializable
data class IncidentDetailKey(val incidentId: Long) : NavKey
