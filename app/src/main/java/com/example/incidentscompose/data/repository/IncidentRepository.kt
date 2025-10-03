package com.example.incidentscompose.data.repository

import com.example.incidentscompose.data.api.IncidentApi
import com.example.incidentscompose.data.model.IncidentResponse

class IncidentRepository(private val incidentApi: IncidentApi) {
    suspend fun getMyIncidents(): Result<List<IncidentResponse>> = incidentApi.getMyIncidents()
}
