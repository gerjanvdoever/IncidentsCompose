package com.example.incidentscompose.data.model

import kotlinx.serialization.Serializable

@Serializable
data class UpdateIncidentRequest(
    val category: IncidentCategory? = null,
    val description: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)
