package com.example.incidentscompose.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.incidentscompose.data.model.IncidentCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ReportIncidentUiState(
    val selectedCategory: IncidentCategory = IncidentCategory.COMMUNAL,
    val description: String = "",
    val photos: List<String> = emptyList(),
    val latitude: Double? = null,
    val longitude: Double? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class ReportIncidentViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ReportIncidentUiState())
    val uiState: StateFlow<ReportIncidentUiState> = _uiState.asStateFlow()

    fun updateCategory(category: IncidentCategory) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun updateDescription(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    fun addPhoto() {
        // TODO: Implement actual photo picker
        // For now, just add a placeholder
        viewModelScope.launch {
            val currentPhotos = _uiState.value.photos
            _uiState.update {
                it.copy(photos = currentPhotos + "photo_${currentPhotos.size + 1}")
            }
        }
    }

    fun removePhoto(photoUri: String) {
        _uiState.update {
            it.copy(photos = it.photos.filter { photo -> photo != photoUri })
        }
    }

    fun useCurrentLocation() {
        // TODO: Implement location fetching
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    latitude = 51.9851,  // Example coordinates
                    longitude = 5.5338
                )
            }
        }
    }

    fun submitReport() {
        val state = _uiState.value

        if (state.description.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter a description") }
            return
        }

        if (state.latitude == null || state.longitude == null) {
            _uiState.update { it.copy(errorMessage = "Please select a location") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                // TODO: Implement actual API call
                // Example:
                // val request = IncidentReportRequest(
                //     category = state.selectedCategory.name,
                //     description = state.description,
                //     latitude = state.latitude,
                //     longitude = state.longitude
                // )
                // repository.submitIncident(request)

                // Simulate network delay
                kotlinx.coroutines.delay(1500)

                // Reset form on success
                _uiState.update {
                    ReportIncidentUiState(
                        selectedCategory = IncidentCategory.COMMUNAL
                    )
                }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to submit report: ${e.message}"
                    )
                }
            }
        }
    }
}