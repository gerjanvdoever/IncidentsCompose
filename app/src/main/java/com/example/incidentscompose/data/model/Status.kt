package com.example.incidentscompose.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class Status {
    REPORTED, ASSIGNED, RESOLVED
}