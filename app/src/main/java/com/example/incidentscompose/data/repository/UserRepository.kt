package com.example.incidentscompose.data.repository

import com.example.incidentscompose.data.api.UserApi
import com.example.incidentscompose.data.model.IncidentResponse
import com.example.incidentscompose.data.model.UpdateUserRequest
import com.example.incidentscompose.data.model.UserResponse

class UserRepository(
    private val userApi: UserApi
) {
    suspend fun register(username: String, password: String, email: String, avatar: String?): Boolean {
        return userApi.register(username, password, email, avatar)
    }

    suspend fun getCurrentUser(): Result<UserResponse> {
        return userApi.getCurrentUser()
    }

    suspend fun updateCurrentUser(updateRequest: UpdateUserRequest): Result<UserResponse> {
        return userApi.updateCurrentUser(updateRequest)
    }

    // ADMIN functions
    suspend fun getAllUsers(): Result<List<UserResponse>> {
        return userApi.getAllUsers()
    }

    suspend fun getUserById(id: Long): Result<UserResponse> {
        return userApi.getUserById(id)
    }

    suspend fun updateUserRole(id: Long, role: String): Result<UserResponse> {
        return userApi.updateUserRole(id, role)
    }

    suspend fun deleteUser(id: Long): Result<Boolean> {
        return userApi.deleteUser(id)
    }

    suspend fun getUserIncidents(id: Long): Result<List<IncidentResponse>> {
        return userApi.getUserIncidents(id)
    }
}