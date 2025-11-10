package com.example.incidentscompose.data.api

import com.example.incidentscompose.data.model.ApiResult
import com.example.incidentscompose.data.model.LoginRequest
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class AuthApi(private val client: HttpClient) {
    private val baseUrl = "http://10.0.2.2:8080/api/auth"

    suspend fun login(username: String, password: String): ApiResult<String> {
        return try {
            val response = client.post("$baseUrl/login") {
                contentType(ContentType.Application.Json)
                setBody(LoginRequest(username, password))
            }

            if (response.status == HttpStatusCode.OK) {
                val body = response.body<Map<String, String>>()
                val token = body["token"]
                if (token != null) {
                    ApiResult.Success(token)
                } else {
                    ApiResult.HttpError(500, "Missing token in response")
                }
            } else {
                ApiResult.HttpError(response.status.value, response.status.description)
            }
        } catch (e: Exception) {
            ApiResult.NetworkError(e)
        }
    }
}
