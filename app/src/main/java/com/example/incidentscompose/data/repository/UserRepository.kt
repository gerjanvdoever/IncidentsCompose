package com.example.incidentscompose.data.repository

import com.example.incidentscompose.data.api.UserApi
import com.example.incidentscompose.data.model.*

class UserRepository(private val userApi: UserApi) {

    suspend fun register(username: String, password: String, email: String, avatar: String?): ApiResult<Unit> =
        userApi.register(username, password, email, avatar) // no auth required

    suspend fun getCurrentUser(): ApiResult<UserResponse> =
        userApi.getCurrentUser()

    suspend fun updateCurrentUser(updateRequest: UpdateUserRequest): ApiResult<UserResponse> =
        userApi.updateCurrentUser(updateRequest)

    // ADMIN functions
    suspend fun getAllUsers(): ApiResult<List<UserResponse>> =
        userApi.getAllUsers()

    suspend fun getUserById(id: Long): ApiResult<UserResponse> =
        userApi.getUserById(id)

    suspend fun updateUserRole(id: Long, role: Role): ApiResult<UserResponse> =
        userApi.updateUserRole(id, role)

    suspend fun deleteUser(id: Long): ApiResult<Unit> =
        userApi.deleteUser(id)

    suspend fun getUserIncidents(id: Long): ApiResult<List<IncidentResponse>> =
        userApi.getUserIncidents(id)
}
