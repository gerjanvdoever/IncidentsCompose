package com.example.incidentscompose

import java.time.LocalDateTime

data class Incident(
    val id: Int,
    val category: String,
    val description: String,
    val priority: Priority,
    val status: String,
    val reportedAt: LocalDateTime
)

enum class Priority(val label: String, val deadlineDays: Long) {
    LOW("Low", 7),
    MEDIUM("Medium", 3),
    HIGH("High", 1)
}
