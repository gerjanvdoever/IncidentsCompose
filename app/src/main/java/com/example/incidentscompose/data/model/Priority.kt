package com.example.incidentscompose.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class Priority {
    LOW, MEDIUM, HIGH, CRITICAL
}