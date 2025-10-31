package com.example.incidentscompose.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class Priority {
    LOW, NORMAL, HIGH, CRITICAL
}