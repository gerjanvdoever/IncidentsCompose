package com.example.incidentscompose.data.model

import kotlinx.serialization.Serializable

@Serializable
data class CreateIncidentRequest(
    val category: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val priority: String = "LOW"
)