package com.example.incidentscompose.data.model

import kotlinx.serialization.Serializable

@Serializable
data class IncidentResponse(
    val id: Long,
    val reportedBy: Long?,
    val category: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val images: List<String>,
    val priority: String,
    val status: String,
    val createdAt: String,
    val updatedAt: String,
    val completedAt: String?,
    val dueAt: String,
    val isAnonymous: Boolean
)