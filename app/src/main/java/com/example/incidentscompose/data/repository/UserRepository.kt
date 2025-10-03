package com.example.incidentscompose.data.repository

import com.example.incidentscompose.data.api.UserApi
import com.example.incidentscompose.data.model.IncidentResponse
import com.example.incidentscompose.data.model.UserResponse

class UserRepository(
    private val userApi: UserApi
)   {
    suspend fun register(username: String, password: String, email: String, avatar: String?): Boolean {
        return userApi.register(username, password, email, avatar)
    }

    suspend fun getCurrentUser(): Result<UserResponse> {
        return userApi.getCurrentUser()
    }

    suspend fun getUserIncidents(userId: String): Result<List<IncidentResponse>> {
        return userApi.getUserIncidents(userId)
    }
}