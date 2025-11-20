package com.example.incidentscompose.util

import com.example.incidentscompose.data.model.IncidentCategory
import com.example.incidentscompose.data.model.Priority
import com.example.incidentscompose.data.model.Role
import com.example.incidentscompose.data.model.Status

object EnumHelper {
    // Priority helpers
    fun getAllPriorities(): List<Priority> = Priority.entries
    fun getPriorityDisplayText(priority: Priority): String = priority.name

    // Status helpers
    fun getAllStatuses(): List<Status> = Status.entries
    fun getStatusDisplayText(status: Status): String = status.name

    // Category helpers
    fun getAllCategories(): List<IncidentCategory> = IncidentCategory.entries
    fun getCategoryDisplayText(category: IncidentCategory): String = category.name

    // User role helpers
    fun getAllUserRoles(): List<Role> = Role.entries
    fun getUserRoleDisplayText(role: Role): String = role.name
}
