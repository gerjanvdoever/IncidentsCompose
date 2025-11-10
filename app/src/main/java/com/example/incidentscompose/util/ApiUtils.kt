package com.example.incidentscompose.util

import com.example.incidentscompose.data.model.ApiResult
import com.example.incidentscompose.data.store.TokenPreferences
import io.ktor.client.call.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend inline fun <reified T> performRequest(
    tokenPreferences: TokenPreferences,
    requiresAuth: Boolean = true,
    crossinline block: suspend (token: String?) -> HttpResponse
): ApiResult<T> = withContext(Dispatchers.IO) {
    val token = if (requiresAuth) tokenPreferences.getToken() else null

    if (requiresAuth && token == null) {
        return@withContext ApiResult.Unauthorized
    }

    try {
        val response = block(token)
        when {
            response.status == HttpStatusCode.Unauthorized -> {
                if (requiresAuth) tokenPreferences.clearToken()
                ApiResult.Unauthorized
            }
            response.status.isSuccess() -> ApiResult.Success(response.body())
            else -> ApiResult.HttpError(response.status.value, response.status.description)
        }
    } catch (e: Exception) {
        ApiResult.NetworkError(e)
    }
}

