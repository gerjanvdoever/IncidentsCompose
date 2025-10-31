package com.example.incidentscompose.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ChangeStatusRequest(
    val status: Status
)
