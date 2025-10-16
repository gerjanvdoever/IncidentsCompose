package com.example.incidentscompose.data.model

import kotlinx.serialization.Serializable

@Serializable
data class RoleUpdateRequest(
    val role: Role
)
