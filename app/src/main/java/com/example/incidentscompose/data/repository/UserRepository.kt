package com.example.incidentscompose.data.repository

import com.example.incidentscompose.data.api.UserApi

class UserRepository(
    private val registerApi: UserApi
) {
    suspend fun register(username: String, password: String, email: String, avatar: String?): Boolean {
        return registerApi.register(username, password, email, avatar)
    }
}