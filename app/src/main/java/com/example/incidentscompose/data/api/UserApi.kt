package com.example.incidentscompose.data.api

import com.example.incidentscompose.data.model.RegisterRequest
import com.example.incidentscompose.data.model.UserResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserApi(private val client: HttpClient) {

    suspend fun register(username: String, password: String, email: String, avatar: String?): Boolean {
        return withContext(Dispatchers.IO) {
            val response: HttpResponse = client.post("http://10.0.2.2:8080/api/users/register") {
                contentType(ContentType.Application.Json)
                setBody(RegisterRequest(username, password, email, avatar))
            }

            response.status == HttpStatusCode.Created
        }
    }
}