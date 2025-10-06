package com.example.incidentscompose.data.repository

import com.example.incidentscompose.data.api.IncidentApi
import com.example.incidentscompose.data.model.CreateIncidentRequest
import com.example.incidentscompose.data.model.ImageUploadResponse
import com.example.incidentscompose.data.model.IncidentResponse
import java.io.File

class IncidentRepository(private val incidentApi: IncidentApi) {
    suspend fun getMyIncidents(): Result<List<IncidentResponse>> = incidentApi.getMyIncidents()

    suspend fun createIncident(createIncidentRequest: CreateIncidentRequest): Result<IncidentResponse> =
        incidentApi.createIncident(createIncidentRequest)

    suspend fun uploadImageToIncident(
        incidentId: Long,
        imageFile: File,
        description: String = ""
    ): Result<ImageUploadResponse> =
        incidentApi.uploadImageToIncident(incidentId, imageFile, description)

    suspend fun uploadMultipleImagesToIncident(
        incidentId: Long,
        imageFiles: List<File>,
        description: String = ""
    ): Result<List<ImageUploadResponse>> =
        incidentApi.uploadMultipleImagesToIncident(incidentId, imageFiles, description)
}
