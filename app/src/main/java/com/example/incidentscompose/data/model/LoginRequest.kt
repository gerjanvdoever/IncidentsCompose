package com.example.incidentscompose.data.model

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(val username: String, val password: String)