package com.example.incidentscompose.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class Role {
    USER, OFFICIAL, ADMIN
}