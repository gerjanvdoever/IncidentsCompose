package com.example.incidentscompose.util

import androidx.compose.ui.graphics.Color
import com.example.incidentscompose.data.model.IncidentCategory
import com.example.incidentscompose.data.model.Status
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

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
            val normalized = if (!dateString.endsWith("Z")) "${dateString}Z" else dateString
            val instant = Instant.parse(normalized)
            val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())

            val day = localDateTime.date.dayOfMonth.toString().padStart(2, '0')
            val month = localDateTime.date.monthNumber.toString().padStart(2, '0')
            val year = localDateTime.date.year

            "$day-$month-$year"
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