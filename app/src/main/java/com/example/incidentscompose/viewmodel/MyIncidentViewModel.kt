package com.example.incidentscompose.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.incidentscompose.data.model.IncidentCategory
import com.example.incidentscompose.data.model.IncidentResponse
import com.example.incidentscompose.data.model.UpdateIncidentRequest
import com.example.incidentscompose.data.model.UserResponse
import com.example.incidentscompose.data.repository.AuthRepository
import com.example.incidentscompose.data.repository.IncidentRepository
import com.example.incidentscompose.data.repository.UserRepository
import com.example.incidentscompose.data.store.IncidentDataStore
import com.example.incidentscompose.data.store.TokenPreferences
import com.example.incidentscompose.ui.states.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MyIncidentViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val incidentRepository: IncidentRepository,
    private val incidentDataStore: IncidentDataStore,
    private val tokenPreferences: TokenPreferences
) : BaseViewModel() {

    private val _user = MutableStateFlow<UserResponse?>(null)
    val user: StateFlow<UserResponse?> = _user.asStateFlow()

    private val _incidents = MutableStateFlow<List<IncidentResponse>>(emptyList())
    val incidents: StateFlow<List<IncidentResponse>> = _incidents.asStateFlow()

    private val _logoutEvent = MutableStateFlow(false)
    val logoutEvent: StateFlow<Boolean> = _logoutEvent.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _updateResult = MutableStateFlow<Result<IncidentResponse>?>(null)
    val updateResult: StateFlow<Result<IncidentResponse>?> = _updateResult.asStateFlow()

    private val _deleteResult = MutableStateFlow<Result<Unit>?>(null)
    val deleteResult: StateFlow<Result<Unit>?> = _deleteResult.asStateFlow()

    private val _userRole = MutableStateFlow<String?>(null)
    val userRole: StateFlow<String?> = _userRole.asStateFlow()

    init {
        loadUserData()
        loadUserRole()
    }

    private fun loadUserRole() {
        viewModelScope.launch {
            _userRole.value = tokenPreferences.getUserRole()
        }
    }

    private fun loadUserData() {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                val userResult = userRepository.getCurrentUser()

                if (userResult.isSuccess) {
                    _user.value = userResult.getOrNull()

                    val incidentsResult = incidentRepository.getMyIncidents()
                    if (incidentsResult.isSuccess) {
                        _incidents.value = incidentsResult.getOrNull() ?: emptyList()
                    }
                } else {
                    logout()
                }
            } catch (e: Exception) {
                logout()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                authRepository.logout()
                _user.value = null
                _incidents.value = emptyList()
                _logoutEvent.value = true
            } catch (e: Exception) {
                // Even if logout fails, clear local state
                _user.value = null
                _incidents.value = emptyList()
                _logoutEvent.value = true
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetLogoutEvent() {
        _logoutEvent.value = false
    }

    // future pull down to refresh for incident list?
    fun refreshIncidents() {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                val incidentsResult = incidentRepository.getMyIncidents()
                if (incidentsResult.isSuccess) {
                    _incidents.value = incidentsResult.getOrNull() ?: emptyList()
                }
            } catch (e: Exception) {
                // Handle error - incidents remain unchanged
            } finally {
                _isLoading.value = false
            }
        }
    }

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

                        refreshIncidents()
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

    fun saveSelectedIncident(incident: IncidentResponse) {
        viewModelScope.launch {
            incidentDataStore.saveSelectedIncident(incident)
        }
    }

    fun getSelectedIncident() = incidentDataStore.selectedIncident

    fun clearSelectedIncident() {
        viewModelScope.launch {
            incidentDataStore.clearSelectedIncident()
        }
    }

    // OPTIONAL LATER: SAVE RECENT INCIDENTS FOR CACHING
    fun saveRecentIncidents(incidents: List<IncidentResponse>) {
        viewModelScope.launch {
            incidentDataStore.saveRecentIncidents(incidents)
        }
    }

    fun getRecentIncidents() = incidentDataStore.recentIncidents
}