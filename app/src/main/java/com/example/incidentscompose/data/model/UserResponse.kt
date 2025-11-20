package com.example.incidentscompose.data.model

import kotlinx.serialization.Serializable

@Serializable
data class UserResponse(
    val id: String,
    val username: String,
    val email: String,
    val role: Role,
    val avatar: String
)