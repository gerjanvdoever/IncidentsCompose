package com.example.incidentscompose.data.api

import com.example.incidentscompose.data.model.IncidentResponse
import com.example.incidentscompose.data.model.RegisterRequest
import com.example.incidentscompose.data.model.Role
import com.example.incidentscompose.data.model.RoleUpdateRequest
import com.example.incidentscompose.data.model.UpdateUserRequest
import com.example.incidentscompose.data.model.UserResponse
import com.example.incidentscompose.data.store.TokenPreferences
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserApi(
    private val client: HttpClient,
    private val tokenPreferences: TokenPreferences
) {
    private val baseUrl = "http://10.0.2.2:8080/api/users"

    suspend fun register(username: String, password: String, email: String, avatar: String?): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val response: HttpResponse = client.post("$baseUrl/register") {
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

                val response: HttpResponse = client.get("$baseUrl/me") {
                    header("Authorization", "Bearer $token")
                }

                if (response.status.isSuccess()) {
                    val userResponse = response.body<UserResponse>()
                    Result.success(userResponse)
                } else {
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

    suspend fun updateCurrentUser(updateRequest: UpdateUserRequest): Result<UserResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val token = tokenPreferences.getToken() ?: ""
                if (token.isEmpty()) {
                    return@withContext Result.failure(Exception("No token available"))
                }

                val response: HttpResponse = client.put("$baseUrl/me") {
                    header("Authorization", "Bearer $token")
                    contentType(ContentType.Application.Json)
                    setBody(updateRequest)
                }

                if (response.status.isSuccess()) {
                    val userResponse = response.body<UserResponse>()
                    Result.success(userResponse)
                } else {
                    if (response.status == HttpStatusCode.Unauthorized) {
                        tokenPreferences.clearToken()
                    }
                    Result.failure(Exception("HTTP ${response.status.value}: ${response.status.description}"))
                }
            } catch (e: Exception) {
                android.util.Log.e("UserApi", "updateCurrentUser failed: ${e.message}", e)
                Result.failure(e)
            }
        }
    }

    // ADMIN endpoints
    suspend fun getAllUsers(): Result<List<UserResponse>> {
        return withContext(Dispatchers.IO) {
            try {
                val token = tokenPreferences.getToken() ?: ""
                if (token.isEmpty()) {
                    return@withContext Result.failure(Exception("No token available"))
                }

                val response: HttpResponse = client.get(baseUrl) {
                    header("Authorization", "Bearer $token")
                }

                if (response.status.isSuccess()) {
                    val users = response.body<List<UserResponse>>()
                    Result.success(users)
                } else {
                    if (response.status == HttpStatusCode.Unauthorized) {
                        tokenPreferences.clearToken()
                    }
                    Result.failure(Exception("HTTP ${response.status.value}: ${response.status.description}"))
                }
            } catch (e: Exception) {
                android.util.Log.e("UserApi", "getAllUsers failed: ${e.message}", e)
                Result.failure(e)
            }
        }
    }

    suspend fun getUserById(id: Long): Result<UserResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val token = tokenPreferences.getToken() ?: ""
                if (token.isEmpty()) {
                    return@withContext Result.failure(Exception("No token available"))
                }

                val response: HttpResponse = client.get("$baseUrl/$id") {
                    header("Authorization", "Bearer $token")
                }

                if (response.status.isSuccess()) {
                    val userResponse = response.body<UserResponse>()
                    Result.success(userResponse)
                } else {
                    if (response.status == HttpStatusCode.Unauthorized) {
                        tokenPreferences.clearToken()
                    }
                    Result.failure(Exception("HTTP ${response.status.value}: ${response.status.description}"))
                }
            } catch (e: Exception) {
                android.util.Log.e("UserApi", "getUserById failed: ${e.message}", e)
                Result.failure(e)
            }
        }
    }

    suspend fun updateUserRole(id: Long, role: String): Result<UserResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val token = tokenPreferences.getToken() ?: ""
                if (token.isEmpty()) {
                    return@withContext Result.failure(Exception("No token available"))
                }

                // Convert string to Role enum
                val roleEnum = try {
                    Role.valueOf(role.uppercase())
                } catch (e: IllegalArgumentException) {
                    return@withContext Result.failure(Exception("Invalid role: $role"))
                }

                val response: HttpResponse = client.put("$baseUrl/$id/role") {
                    header("Authorization", "Bearer $token")
                    contentType(ContentType.Application.Json)
                    setBody(RoleUpdateRequest(roleEnum))
                }

                if (response.status.isSuccess()) {
                    val userResponse = response.body<UserResponse>()
                    Result.success(userResponse)
                } else {
                    if (response.status == HttpStatusCode.Unauthorized) {
                        tokenPreferences.clearToken()
                    }
                    Result.failure(Exception("HTTP ${response.status.value}: ${response.status.description}"))
                }
            } catch (e: Exception) {
                android.util.Log.e("UserApi", "updateUserRole failed: ${e.message}", e)
                Result.failure(e)
            }
        }
    }

    suspend fun deleteUser(id: Long): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val token = tokenPreferences.getToken() ?: ""
                if (token.isEmpty()) {
                    return@withContext Result.failure(Exception("No token available"))
                }

                val response: HttpResponse = client.delete("$baseUrl/$id") {
                    header("Authorization", "Bearer $token")
                }

                if (response.status == HttpStatusCode.NoContent) {
                    Result.success(true)
                } else {
                    if (response.status == HttpStatusCode.Unauthorized) {
                        tokenPreferences.clearToken()
                    }
                    Result.failure(Exception("HTTP ${response.status.value}: ${response.status.description}"))
                }
            } catch (e: Exception) {
                android.util.Log.e("UserApi", "deleteUser failed: ${e.message}", e)
                Result.failure(e)
            }
        }
    }

    suspend fun getUserIncidents(id: Long): Result<List<IncidentResponse>> {
        return withContext(Dispatchers.IO) {
            try {
                val token = tokenPreferences.getToken() ?: ""
                if (token.isEmpty()) {
                    return@withContext Result.failure(Exception("No token available"))
                }

                val response: HttpResponse = client.get("$baseUrl/$id/incidents") {
                    header("Authorization", "Bearer $token")
                }

                if (response.status.isSuccess()) {
                    val incidents = response.body<List<IncidentResponse>>()
                    Result.success(incidents)
                } else {
                    if (response.status == HttpStatusCode.Unauthorized) {
                        tokenPreferences.clearToken()
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