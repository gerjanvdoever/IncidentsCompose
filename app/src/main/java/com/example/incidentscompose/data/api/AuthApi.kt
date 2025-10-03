package com.example.incidentscompose.data.api

import LoginRequest
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthApi(private val client: HttpClient) {

    suspend fun login(username: String, password: String): String? {
        return withContext(Dispatchers.IO) {
            val response: HttpResponse = client.post("http://10.0.2.2:8080/api/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(LoginRequest(username, password))
            }

            if (response.status == HttpStatusCode.OK) {
                val body: Map<String, String> = response.body()
                body["token"]
            } else {
                null
            }
        }
    }
}