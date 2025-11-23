package com.example.incidentscompose.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.incidentscompose.data.model.*
import com.example.incidentscompose.data.repository.IncidentRepository
import com.example.incidentscompose.data.store.IncidentDataStore
import com.example.incidentscompose.ui.states.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MyIncidentDetailViewModel(
    private val incidentRepository: IncidentRepository,
    private val incidentDataStore: IncidentDataStore
) : BaseViewModel() {

    private val _updateResult = MutableStateFlow<ApiResult<IncidentResponse>?>(null)
    val updateResult: StateFlow<ApiResult<IncidentResponse>?> = _updateResult.asStateFlow()

    private val _deleteResult = MutableStateFlow<ApiResult<Unit>?>(null)
    val deleteResult: StateFlow<ApiResult<Unit>?> = _deleteResult.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    val selectedIncident = incidentDataStore.selectedIncident

    fun updateIncident(
        incidentId: Long,
        category: IncidentCategory? = null,
        description: String? = null,
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

                    when (result) {
                        is ApiResult.Success -> incidentDataStore.saveSelectedIncident(result.data)
                        is ApiResult.Timeout -> _toastMessage.value = "Update request timed out."
                        is ApiResult.Unknown -> _toastMessage.value = "Unexpected error while updating."
                        is ApiResult.HttpError -> _toastMessage.value = "Failed to update incident"
                        is ApiResult.NetworkError -> _toastMessage.value =
                            "Network error: ${result.exception.message ?: "Please try again"}"
                        is ApiResult.Unauthorized -> _toastMessage.value = "Unauthorized action."
                    }
                } catch (e: Exception) {
                    _updateResult.value = ApiResult.NetworkError(e)
                    _toastMessage.value = "Unexpected error: ${e.message ?: "Please try again"}"
                }
            }
        }
    }

    fun deleteIncident(incidentId: Long) {
        viewModelScope.launch {
            withLoading {
                try {
                    val result = incidentRepository.deleteIncident(incidentId)
                    _deleteResult.value = result

                    when (result) {
                        is ApiResult.Success -> _toastMessage.value = "Incident deleted successfully."
                        is ApiResult.Timeout -> _toastMessage.value = "Delete request timed out."
                        is ApiResult.Unknown -> _toastMessage.value = "Unexpected error while deleting."
                        is ApiResult.HttpError -> _toastMessage.value = "Failed to delete incident"
                        is ApiResult.NetworkError -> _toastMessage.value =
                            "Network error: ${result.exception.message ?: "Please try again"}"
                        is ApiResult.Unauthorized -> _toastMessage.value = "Unauthorized action."
                    }
                } catch (e: Exception) {
                    _deleteResult.value = ApiResult.NetworkError(e)
                    _toastMessage.value = "Unexpected error: ${e.message ?: "Please try again"}"
                }
            }
        }
    }

    fun resetUpdateResult() {
        _updateResult.value = null
    }

    fun resetDeleteResult() {
        _deleteResult.value = null
    }

    fun clearSelectedIncident() {
        viewModelScope.launch {
            incidentDataStore.clearSelectedIncident()
        }
    }

    fun clearToastMessage() {
        _toastMessage.value = null
    }
}
