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
        return try {
            withContext(Dispatchers.IO) {
                val response: HttpResponse = client.post("http://10.0.2.2:8080/api/users/register") {
                    contentType(ContentType.Application.Json)
                    setBody(RegisterRequest(username, password, email, avatar))
                }

                println("Registration response status: ${response.status}")

                if (response.status == HttpStatusCode.Created) {
                    // Try to parse the response body to verify it's working
                    try {
                        val userResponse: UserResponse = response.body()
                        println("Registration successful. User created: ${userResponse.username} with ID: ${userResponse.id}")
                        true
                    } catch (parseError: Exception) {
                        // Even if parsing fails, if status is 201, registration was successful
                        println("Registration successful but parsing failed: ${parseError.message}")
                        true
                    }
                } else {
                    // Try to get error message from response
                    val errorBody: String = response.body()
                    println("Registration failed with status: ${response.status}. Error: $errorBody")
                    false
                }
            }
        } catch (e: Exception) {
            println("Registration exception: ${e.message}")
            e.printStackTrace()
            false
        }
    }
}