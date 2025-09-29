package com.example.incidentscompose.data.repository

import com.example.incidentscompose.data.api.AuthApi
import com.example.incidentscompose.data.store.TokenPreferences

class AuthRepository(
    private val authApi: AuthApi,
    private val tokenPreferences: TokenPreferences
) {
    suspend fun login(username: String, password: String): Boolean {
        val token = authApi.login(username, password)
        return if (token != null) {
            tokenPreferences.saveToken(token)
            true
        } else {
            false
        }
    }

    suspend fun getSavedToken(): String? = tokenPreferences.getToken()

    suspend fun logout() {
        tokenPreferences.clearToken()
    }
}
