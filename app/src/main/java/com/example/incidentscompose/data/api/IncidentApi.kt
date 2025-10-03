package com.example.incidentscompose.data.api

import com.example.incidentscompose.data.model.IncidentResponse
import com.example.incidentscompose.data.store.TokenPreferences
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.Result

class IncidentApi(
    private val client: HttpClient,
    private val tokenPreferences: TokenPreferences
) {
    suspend fun getMyIncidents(): Result<List<IncidentResponse>> {
        return withContext(Dispatchers.IO) {
            try {
                val token = tokenPreferences.getToken() ?: ""
                if (token.isEmpty()) {
                    return@withContext Result.failure(Exception("No token available"))
                }

                val response: HttpResponse = client.get("http://10.0.2.2:8080/api/incidents/my-incidents") {
                    header("Authorization", "Bearer $token")
                }

                println("Response status: ${response.status}")
                val rawResponse = response.bodyAsText()
                println("Raw response: $rawResponse")

                if (response.status.isSuccess()) {
                    val incidents: List<IncidentResponse> = response.body()
                    println("Parsed incidents: ${incidents.size}")
                    incidents.forEach { println("Incident: $it") }
                    Result.success(incidents)
                } else {
                    if (response.status == HttpStatusCode.Unauthorized) {
                        tokenPreferences.clearToken()
                    }
                    Result.failure(Exception("HTTP ${response.status.value}: ${response.status.description}"))
                }
            } catch (e: Exception) {
                println("Error in getMyIncidents: ${e.message}")
                e.printStackTrace()
                if (e is io.ktor.serialization.JsonConvertException) {
                    tokenPreferences.clearToken()
                }
                Result.failure(e)
            }
        }
    }
}
