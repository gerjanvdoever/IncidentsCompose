package com.example.incidentscompose.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.incidentscompose.data.model.IncidentCategory
import com.example.incidentscompose.data.model.IncidentResponse
import com.example.incidentscompose.data.model.UpdateIncidentRequest
import com.example.incidentscompose.data.repository.IncidentRepository
import com.example.incidentscompose.data.store.IncidentDataStore
import com.example.incidentscompose.ui.states.BaseViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MyIncidentDetailViewModel(
    private val incidentRepository: IncidentRepository,
    private val incidentDataStore: IncidentDataStore
) : BaseViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _updateResult = MutableStateFlow<Result<IncidentResponse>?>(null)
    val updateResult: StateFlow<Result<IncidentResponse>?> = _updateResult.asStateFlow()

    private val _deleteResult = MutableStateFlow<Result<Unit>?>(null)
    val deleteResult: StateFlow<Result<Unit>?> = _deleteResult.asStateFlow()

    fun updateIncident(
        incidentId: Long,
        category: IncidentCategory?,
        description: String?,
        latitude: Double? = null,
        longitude: Double? = null
    ) {
        viewModelScope.launch {
            withLoading {
                try {
                    val updateRequest = UpdateIncidentRequest(
                        category = category,
                        description = description,
                        latitude = latitude,
                        longitude = longitude
                    )

                    val result = incidentRepository.updateIncident(incidentId, updateRequest)
                    _updateResult.value = result

                    if (result.isSuccess) {
                        result.getOrNull()?.let { updatedIncident ->
                            incidentDataStore.saveSelectedIncident(updatedIncident)
                        }
                    }
                } catch (e: Exception) {
                    _updateResult.value = Result.failure(e)
                }
            }
        }
    }

    fun resetUpdateResult() {
        _updateResult.value = null
    }

    fun deleteIncident(incidentId: Long) {
        viewModelScope.launch {
            withLoading {
                try {
                    val result = incidentRepository.deleteIncident(incidentId)
                    _deleteResult.value = result
                } catch (e: Exception) {
                    _deleteResult.value = Result.failure(e)
                }
            }
        }
    }

    fun resetDeleteResult() {
        _deleteResult.value = null
    }

    fun getSelectedIncident() = incidentDataStore.selectedIncident

    fun clearSelectedIncident() {
        viewModelScope.launch {
            incidentDataStore.clearSelectedIncident()
        }
    }
}