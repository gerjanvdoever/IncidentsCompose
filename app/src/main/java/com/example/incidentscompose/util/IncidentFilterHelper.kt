package com.example.incidentscompose.util

import com.example.incidentscompose.data.model.IncidentResponse

class IncidentFilterHelper {

    fun filterIncidents(
        incidents: List<IncidentResponse>,
        searchQuery: String = "",
        priorityFilter: Set<String> = emptySet(),
        statusFilter: Set<String> = emptySet(),
        categoryFilter: Set<String> = emptySet()
    ): List<IncidentResponse> {
        return incidents.filter { incident ->
            val matchesSearch = searchQuery.isEmpty() ||
                    incident.category.contains(searchQuery, ignoreCase = true) ||
                    incident.description.contains(searchQuery, ignoreCase = true) ||
                    incident.priority.contains(searchQuery, ignoreCase = true) ||
                    incident.status.contains(searchQuery, ignoreCase = true)

            val matchesPriority = priorityFilter.isEmpty() ||
                    priorityFilter.any { it.equals(incident.priority, ignoreCase = true) }

            val matchesStatus = statusFilter.isEmpty() ||
                    statusFilter.any { it.equals(incident.status, ignoreCase = true) }

            val matchesCategory = categoryFilter.isEmpty() ||
                    categoryFilter.any { it.equals(incident.category, ignoreCase = true) }

            matchesSearch && matchesPriority && matchesStatus && matchesCategory
        }
    }

    fun hasActiveFilters(
        searchQuery: String = "",
        priorityFilter: Set<String> = emptySet(),
        statusFilter: Set<String> = emptySet(),
        categoryFilter: Set<String> = emptySet()
    ): Boolean {
        return searchQuery.isNotEmpty() ||
                priorityFilter.isNotEmpty() ||
                statusFilter.isNotEmpty() ||
                categoryFilter.isNotEmpty()
    }

    companion object {
        val instance = IncidentFilterHelper()
    }
}