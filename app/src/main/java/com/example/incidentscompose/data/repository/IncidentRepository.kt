package com.example.incidentscompose.data.repository

import com.example.incidentscompose.data.api.IncidentApi
import com.example.incidentscompose.data.model.CreateIncidentRequest
import com.example.incidentscompose.data.model.ImageUploadResponse
import com.example.incidentscompose.data.model.IncidentResponse
import com.example.incidentscompose.data.model.PaginatedItemResponse
import com.example.incidentscompose.data.model.Priority
import com.example.incidentscompose.data.model.Status
import com.example.incidentscompose.data.model.UpdateIncidentRequest
import java.io.File

class IncidentRepository(private val incidentApi: IncidentApi) {

    // Existing methods
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

    // Get all incidents (Admin/Official only)
    suspend fun getAllIncidents(): Result<List<IncidentResponse>> = incidentApi.getAllIncidents()

    // Get paginated incidents
    suspend fun getPaginatedIncidents(page: Int = 1, pageSize: Int = 10): Result<PaginatedItemResponse<IncidentResponse>> =
        incidentApi.getPaginatedIncidents(page, pageSize)

    // Get incident by ID
    suspend fun getIncidentById(incidentId: Long): Result<IncidentResponse> =
        incidentApi.getIncidentById(incidentId)

    // Update incident
    suspend fun updateIncident(incidentId: Long, updateRequest: UpdateIncidentRequest): Result<IncidentResponse> =
        incidentApi.updateIncident(incidentId, updateRequest)

    // Delete incident
    suspend fun deleteIncident(incidentId: Long): Result<Unit> =
        incidentApi.deleteIncident(incidentId)

    // Change incident priority (Admin/Official only)
    suspend fun changeIncidentPriority(incidentId: Long, priority: Priority): Result<IncidentResponse> =
        incidentApi.changeIncidentPriority(incidentId, priority)

    // Change incident status (Admin/Official only)
    suspend fun changeIncidentStatus(incidentId: Long, status: Status): Result<IncidentResponse> =
        incidentApi.changeIncidentStatus(incidentId, status)
}