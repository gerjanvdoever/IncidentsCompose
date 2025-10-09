package com.example.incidentscompose.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.incidentscompose.data.model.IncidentResponse
import com.example.incidentscompose.data.model.UserResponse
import com.example.incidentscompose.data.repository.AuthRepository
import com.example.incidentscompose.data.repository.IncidentRepository
import com.example.incidentscompose.data.repository.UserRepository
import com.example.incidentscompose.data.store.IncidentDataStore
import com.example.incidentscompose.ui.states.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MyIncidentViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val incidentRepository: IncidentRepository,
    private val incidentDataStore: IncidentDataStore
) : BaseViewModel() {

    private val _user = MutableStateFlow<UserResponse?>(null)
    val user: StateFlow<UserResponse?> = _user.asStateFlow()

    private val _incidents = MutableStateFlow<List<IncidentResponse>>(emptyList())
    val incidents: StateFlow<List<IncidentResponse>> = _incidents.asStateFlow()

    private val _logoutEvent = MutableStateFlow(false)
    val logoutEvent: StateFlow<Boolean> = _logoutEvent.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadUserData()
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