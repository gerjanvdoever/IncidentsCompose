package com.example.incidentscompose.viewmodel

import android.content.Context
import androidx.lifecycle.viewModelScope
import androidx.core.net.toUri
import com.example.incidentscompose.data.model.*
import com.example.incidentscompose.data.repository.IncidentRepository
import com.example.incidentscompose.ui.states.BaseViewModel
import com.example.incidentscompose.util.PhotoUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ReportIncidentUiState(
    val selectedCategory: IncidentCategory = IncidentCategory.COMMUNAL,
    val description: String = "",
    val photos: List<String> = emptyList(),
    val latitude: Double? = null,
    val longitude: Double? = null,
    val errorMessage: String? = null,
    val showSuccessDialog: Boolean = false,
    val createdIncident: IncidentResponse? = null,
    val showPermissionDeniedWarning: Boolean = false,
    val showImageSourceDialog: Boolean = false,
    val hasPermissions: Boolean = false
)

class ReportIncidentViewModel(
    private val repository: IncidentRepository
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(ReportIncidentUiState())
    val uiState = _uiState.asStateFlow()

    fun updateCategory(category: IncidentCategory) =
        _uiState.update { it.copy(selectedCategory = category) }

    fun updateDescription(description: String) =
        _uiState.update { it.copy(description = description) }

    fun addPhoto(uri: String) =
        _uiState.update { it.copy(photos = it.photos + uri) }

    fun removePhoto(uri: String) =
        _uiState.update { it.copy(photos = it.photos - uri) }

    fun useCurrentLocation() {
        _uiState.update { it.copy(latitude = 51.9851, longitude = 5.5338) }
    }

    fun updateLocation(latitude: Double, longitude: Double) =
        _uiState.update { it.copy(latitude = latitude, longitude = longitude) }

    fun clearLocation() =
        _uiState.update { it.copy(latitude = null, longitude = null) }

    fun updatePermissions(granted: Boolean) {
        _uiState.update { it.copy(hasPermissions = granted) }
        if (!granted) showPermissionDeniedWarning()
    }

    private fun showPermissionDeniedWarning() =
        _uiState.update { it.copy(showPermissionDeniedWarning = true) }

    fun dismissPermissionWarning() =
        _uiState.update { it.copy(showPermissionDeniedWarning = false) }

    fun showImageSourceDialog() =
        _uiState.update { it.copy(showImageSourceDialog = true) }

    fun dismissImageSourceDialog() =
        _uiState.update { it.copy(showImageSourceDialog = false) }

    fun submitReport(context: Context) {
        val state = _uiState.value

        when {
            state.description.isBlank() -> {
                _uiState.update { it.copy(errorMessage = "Please enter a description") }
                return
            }
            state.latitude == null || state.longitude == null -> {
                _uiState.update { it.copy(errorMessage = "Please select a location") }
                return
            }
        }

        viewModelScope.launch {
            withLoading {
                try {
                    // Create the incident
                    when (val result = repository.createIncident(
                        CreateIncidentRequest(
                            category = state.selectedCategory.name,
                            description = state.description,
                            latitude = state.latitude,
                            longitude = state.longitude,
                            priority = "LOW"
                        )
                    )) {
                        is ApiResult.Success -> {
                            val incident = result.data

                            // Upload photos
                            state.photos.forEach { uriString ->
                                val file = PhotoUtils.getFileFromUri(context, uriString.toUri())
                                if (file != null) {
                                    when (val uploadResult = repository.uploadImageToIncident(
                                        incidentId = incident.id,
                                        imageFile = file,
                                        description = ""
                                    )) {
                                        is ApiResult.Success -> Unit
                                        is ApiResult.HttpError ->
                                            _uiState.update { it.copy(errorMessage = "Failed to upload image: ${file.name}") }
                                        is ApiResult.NetworkError ->
                                            _uiState.update { it.copy(errorMessage = "Network error uploading image: ${file.name}") }
                                        is ApiResult.Timeout ->
                                            _uiState.update { it.copy(errorMessage = "Image upload timed out: ${file.name}") }
                                        is ApiResult.Unknown ->
                                            _uiState.update { it.copy(errorMessage = "Unknown error uploading image: ${file.name}") }
                                        is ApiResult.Unauthorized -> Unit // Should never happen here
                                    }
                                }
                            }

                            _uiState.update {
                                it.copy(
                                    showSuccessDialog = true,
                                    createdIncident = incident,
                                    errorMessage = null
                                )
                            }
                        }
                        is ApiResult.HttpError -> _uiState.update {
                            it.copy(errorMessage = "Failed to report incident: ${result.message}")
                        }
                        is ApiResult.NetworkError -> _uiState.update {
                            it.copy(errorMessage = "Network error: ${result.exception.message}")
                        }
                        is ApiResult.Timeout -> _uiState.update {
                            it.copy(errorMessage = "Request timed out. Please try again.")
                        }
                        is ApiResult.Unknown -> _uiState.update {
                            it.copy(errorMessage = "Unexpected error occurred while reporting incident.")
                        }
                        is ApiResult.Unauthorized -> Unit // Should never happen
                    }
                } catch (e: Exception) {
                    _uiState.update {
                        it.copy(errorMessage = "Unexpected error: ${e.message ?: "Please try again"}")
                    }
                }
            }
        }
    }


    fun dismissSuccessDialog() =
        _uiState.update { it.copy(showSuccessDialog = false) }
}
