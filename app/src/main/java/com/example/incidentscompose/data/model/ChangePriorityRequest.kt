package com.example.incidentscompose.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ChangePriorityRequest(
    val priority: Priority
)
