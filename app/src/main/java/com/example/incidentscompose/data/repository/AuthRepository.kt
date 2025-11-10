package com.example.incidentscompose.data.repository

import com.example.incidentscompose.data.api.AuthApi
import com.example.incidentscompose.data.model.ApiResult
import com.example.incidentscompose.data.store.TokenPreferences

class AuthRepository(
    private val authApi: AuthApi,
    private val tokenPreferences: TokenPreferences
) {
    suspend fun login(username: String, password: String): ApiResult<Unit> {
        return when (val result = authApi.login(username, password)) {
            is ApiResult.Success -> {
                tokenPreferences.saveToken(result.data)
                ApiResult.Success(Unit)
            }
            is ApiResult.HttpError -> ApiResult.HttpError(result.code, result.message)
            is ApiResult.NetworkError -> ApiResult.NetworkError(result.exception)
            ApiResult.Unauthorized -> ApiResult.Unauthorized // Will never throw
        }
    }

    suspend fun getSavedToken(): String? = tokenPreferences.getToken()

    suspend fun logout() {
        tokenPreferences.clearToken()

    }
}
