package com.example.incidentscompose.data.model

import kotlinx.serialization.Serializable

@Serializable
data class IncidentResponse(
    val id: Long,
    val reportedBy: Long?,
    val category: String,        // This will be our main identifier
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val images: List<String>,
    val priority: String,        // Keep as String for now
    val status: String,          // Keep as String for now
    val createdAt: String,
    val updatedAt: String,
    val completedAt: String?,
    val dueAt: String,
    val isAnonymous: Boolean
)