package com.example.incidentscompose.util

import androidx.compose.ui.graphics.Color
import com.example.incidentscompose.data.model.IncidentCategory
import com.example.incidentscompose.data.model.Status

object IncidentDisplayHelper {
    fun getStatusColor(status: Status): Color {
        return when (status) {
            Status.REPORTED -> Color(0xFFFFC107)
            Status.ASSIGNED -> Color(0xFFFF6B35)
            Status.RESOLVED -> Color(0xFF4CAF50)
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


    fun formatCategoryText(category: IncidentCategory): String {
        val name = category.name
        return if (name.isNotEmpty()) {
            name.lowercase().replaceFirstChar { it.uppercase() }
        } else {
            name
        }
    }
}