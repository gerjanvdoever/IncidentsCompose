package com.example.incidentscompose.data.model

data class UpdateIncidentRequest(
    val category: IncidentCategory? = null,
    val description: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)
