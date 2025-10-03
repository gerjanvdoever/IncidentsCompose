package com.example.incidentscompose.data.api

import com.example.incidentscompose.data.model.IncidentResponse
import com.example.incidentscompose.data.model.RegisterRequest
import com.example.incidentscompose.data.model.UserResponse
import com.example.incidentscompose.data.store.TokenPreferences
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.Result

class UserApi(
    private val client: HttpClient,
    private val tokenPreferences: TokenPreferences
) {

    suspend fun register(username: String, password: String, email: String, avatar: String?): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val response: HttpResponse = client.post("http://10.0.2.2:8080/api/users/register") {
                    contentType(ContentType.Application.Json)
                    setBody(RegisterRequest(username, password, email, avatar))
                }
                response.status == HttpStatusCode.Created
            } catch (e: Exception) {
                android.util.Log.e("UserApi", "Registration failed", e)
                false
            }
        }
    }

    suspend fun getCurrentUser(): Result<UserResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val token = tokenPreferences.getToken() ?: ""
                if (token.isEmpty()) {
                    return@withContext Result.failure(Exception("No token available"))
                }

                val response: HttpResponse = client.get("http://10.0.2.2:8080/api/users/me") {
                    header("Authorization", "Bearer $token")
                }

                if (response.status.isSuccess()) {
                    val userResponse = response.body<UserResponse>()
                    Result.success(userResponse)
                } else {
                    // If we get 401 Unauthorized, clear the token
                    if (response.status == HttpStatusCode.Unauthorized) {
                        tokenPreferences.clearToken()
                        android.util.Log.w("UserApi", "Token invalid, clearing stored token")
                    }
                    Result.failure(Exception("HTTP ${response.status.value}: ${response.status.description}"))
                }
            } catch (e: Exception) {
                android.util.Log.e("UserApi", "getCurrentUser failed: ${e.message}", e)
                Result.failure(e)
            }
        }
    }

    suspend fun getUserIncidents(userId: String): Result<List<IncidentResponse>> {
        return withContext(Dispatchers.IO) {
            try {
                val token = tokenPreferences.getToken() ?: ""
                if (token.isEmpty()) {
                    return@withContext Result.failure(Exception("No token available"))
                }

                val response: HttpResponse = client.get("http://10.0.2.2:8080/api/users/$userId/incidents") {
                    header("Authorization", "Bearer $token")
                }

                if (response.status.isSuccess()) {
                    val incidents = response.body<List<IncidentResponse>>()
                    Result.success(incidents)
                } else {
                    if (response.status == HttpStatusCode.Unauthorized) {
                        tokenPreferences.clearToken()
                        android.util.Log.w("UserApi", "Token invalid, clearing stored token")
                    }
                    Result.failure(Exception("HTTP ${response.status.value}: ${response.status.description}"))
                }
            } catch (e: Exception) {
                android.util.Log.e("UserApi", "getUserIncidents failed: ${e.message}", e)

                Result.failure(e)
            }
        }
    }
}