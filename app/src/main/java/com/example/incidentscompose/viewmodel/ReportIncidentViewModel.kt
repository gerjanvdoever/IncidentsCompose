package com.example.incidentscompose.viewmodel

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.example.incidentscompose.data.model.CreateIncidentRequest
import com.example.incidentscompose.data.model.IncidentCategory
import com.example.incidentscompose.data.model.IncidentResponse
import com.example.incidentscompose.data.repository.IncidentRepository
import com.example.incidentscompose.ui.states.BaseViewModel
import com.example.incidentscompose.util.PhotoUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import androidx.core.net.toUri

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
        _uiState.update {
            it.copy(latitude = 51.9851, longitude = 5.5338)
        }
    }

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
                runCatching {
                    val request = CreateIncidentRequest(
                        category = state.selectedCategory.name,
                        description = state.description,
                        latitude = state.latitude,
                        longitude = state.longitude,
                        priority = "LOW"
                    )

                    val incident = repository.createIncident(request).getOrThrow()

                    state.photos.forEach { uriString ->
                        val file = PhotoUtils.getFileFromUri(context, uriString.toUri())
                        if (file != null) {
                            repository.uploadImageToIncident(
                                incidentId = incident.id,
                                imageFile = file,
                                description = ""
                            )
                        }
                    }

                    _uiState.update {
                        it.copy(
                            showSuccessDialog = true,
                            createdIncident = incident,
                            errorMessage = null
                        )
                    }
                }.onFailure { e ->
                    _uiState.update {
                        it.copy(
                            errorMessage = e.message ?: "Failed to submit report"
                        )
                    }
                }
            }
        }
    }

    fun dismissSuccessDialog() =
        _uiState.update { it.copy(showSuccessDialog = false) }
}