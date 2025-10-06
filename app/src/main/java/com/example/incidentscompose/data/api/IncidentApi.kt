package com.example.incidentscompose.data.api

import com.example.incidentscompose.data.model.CreateIncidentRequest
import com.example.incidentscompose.data.model.ImageUploadResponse
import com.example.incidentscompose.data.model.IncidentResponse
import com.example.incidentscompose.data.store.TokenPreferences
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.Result

class IncidentApi(
    private val client: HttpClient,
    private val tokenPreferences: TokenPreferences
) {

    suspend fun getMyIncidents(): Result<List<IncidentResponse>> = withContext(Dispatchers.IO) {
        try {
            val token = tokenPreferences.getToken() ?: ""
            if (token.isEmpty()) return@withContext Result.failure(Exception("No token available"))

            val response: HttpResponse = client.get("http://10.0.2.2:8080/api/incidents/my-incidents") {
                header("Authorization", "Bearer $token")
            }

            if (response.status.isSuccess()) {
                val incidents: List<IncidentResponse> = response.body()
                Result.success(incidents)
            } else {
                if (response.status == HttpStatusCode.Unauthorized) tokenPreferences.clearToken()
                Result.failure(Exception("HTTP ${response.status.value}: ${response.status.description}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createIncident(createIncidentRequest: CreateIncidentRequest): Result<IncidentResponse> = withContext(Dispatchers.IO) {
        try {
            val token = tokenPreferences.getToken() ?: ""

            val response: HttpResponse = client.post("http://10.0.2.2:8080/api/incidents") {
                header("Authorization", "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(createIncidentRequest)
            }

            if (response.status.isSuccess()) {
                val createdIncident: IncidentResponse = response.body()
                Result.success(createdIncident)
            } else {
                if (response.status == HttpStatusCode.Unauthorized) tokenPreferences.clearToken()
                Result.failure(Exception("HTTP ${response.status.value}: ${response.status.description}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadImageToIncident(
        incidentId: Long,
        imageFile: File,
        description: String = ""
    ): Result<ImageUploadResponse> = withContext(Dispatchers.IO) {
        try {
            val token = tokenPreferences.getToken() ?: ""
            if (token.isEmpty()) return@withContext Result.failure(Exception("No token available"))
            if (!imageFile.exists()) return@withContext Result.failure(Exception("Image file does not exist"))

            val response: HttpResponse = client.post("http://10.0.2.2:8080/api/incidents/$incidentId/images") {
                header("Authorization", "Bearer $token")
                setBody(MultiPartFormDataContent(
                    formData {
                        if (description.isNotEmpty()) append("description", description)
                        append(
                            "image",
                            imageFile.readBytes(),
                            headers = Headers.build {
                                append(HttpHeaders.ContentDisposition, "form-data; name=\"image\"; filename=\"${imageFile.name}\"")
                                append(HttpHeaders.ContentType, "image/jpeg")
                            }
                        )
                    }
                ))
            }

            if (response.status.isSuccess()) {
                Result.success(ImageUploadResponse(response.bodyAsText()))
            } else {
                if (response.status == HttpStatusCode.Unauthorized) tokenPreferences.clearToken()
                Result.failure(Exception("HTTP ${response.status.value}: ${response.status.description}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadMultipleImagesToIncident(
        incidentId: Long,
        imageFiles: List<File>,
        description: String = ""
    ): Result<List<ImageUploadResponse>> = withContext(Dispatchers.IO) {
        try {
            val token = tokenPreferences.getToken() ?: ""
            if (token.isEmpty()) return@withContext Result.failure(Exception("No token available"))

            val validFiles = imageFiles.filter { it.exists() }
            if (validFiles.isEmpty()) return@withContext Result.failure(Exception("No valid image files provided"))

            val response: HttpResponse = client.post("http://10.0.2.2:8080/api/incidents/$incidentId/images") {
                header("Authorization", "Bearer $token")
                setBody(MultiPartFormDataContent(
                    formData {
                        if (description.isNotEmpty()) append("description", description)
                        validFiles.forEachIndexed { index, file ->
                            append(
                                "image$index",
                                file.readBytes(),
                                headers = Headers.build {
                                    append(HttpHeaders.ContentDisposition, "form-data; name=\"image$index\"; filename=\"${file.name}\"")
                                    append(HttpHeaders.ContentType, getContentTypeForFile(file))
                                }
                            )
                        }
                    }
                ))
            }

            if (response.status.isSuccess()) {
                Result.success(listOf(ImageUploadResponse(response.bodyAsText())))
            } else {
                if (response.status == HttpStatusCode.Unauthorized) tokenPreferences.clearToken()
                Result.failure(Exception("HTTP ${response.status.value}: ${response.status.description}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun getContentTypeForFile(file: File): String {
        return when (file.extension.lowercase()) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            "webp" -> "image/webp"
            else -> "application/octet-stream"
        }
    }
}
