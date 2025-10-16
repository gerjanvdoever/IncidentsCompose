package com.example.incidentscompose.util

import com.example.incidentscompose.data.model.IncidentCategory

object IncidentCategoryUtils {
    fun safeValueOf(category: String): IncidentCategory {
        return try {
            // Try to match exactly first
            enumValueOf<IncidentCategory>(category.uppercase())
        } catch (e: IllegalArgumentException) {
            // If exact match fails, try case-insensitive matching
            IncidentCategory.entries.find {
                it.name.equals(category, ignoreCase = true)
            } ?: IncidentCategory.OTHER
        }
    }
}