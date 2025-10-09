package com.example.incidentscompose.util

import androidx.compose.ui.graphics.Color

object IncidentDisplayHelper {
    fun getStatusColor(status: String): Color {
        return when (status.uppercase()) {
            "ASSIGNED" -> Color(0xFFFF6B35)
            "RESOLVED" -> Color(0xFF4CAF50)
            "REPORTED" -> Color(0xFFFFC107)
            else -> Color.Gray
        }
    }

    fun formatDateForDisplay(dateString: String): String {
        return try {
            if (dateString.contains("T")) {
                dateString.split("T").first()
            } else {
                dateString
            }
        } catch (e: Exception) {
            dateString
        }
    }

    fun formatCategoryText(category: String): String {
        return if (category.isNotEmpty()) {
            category.lowercase().replaceFirstChar { it.uppercase() }
        } else {
            category
        }
    }
}