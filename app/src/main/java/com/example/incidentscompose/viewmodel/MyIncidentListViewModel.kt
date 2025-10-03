package com.example.incidentscompose.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.incidentscompose.data.model.IncidentResponse
import com.example.incidentscompose.data.model.UserResponse
import com.example.incidentscompose.data.repository.AuthRepository
import com.example.incidentscompose.data.repository.IncidentRepository
import com.example.incidentscompose.data.repository.UserRepository
import com.example.incidentscompose.ui.states.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MyIncidentListViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val incidentRepository: IncidentRepository
) : BaseViewModel() {

    private val _user = MutableStateFlow<UserResponse?>(null)
    val user: StateFlow<UserResponse?> = _user.asStateFlow()

    private val _incidents = MutableStateFlow<List<IncidentResponse>>(emptyList())
    val incidents: StateFlow<List<IncidentResponse>> = _incidents.asStateFlow()

    // Event to notify the Composable to navigate after logout
    private val _logoutEvent = MutableStateFlow(false)
    val logoutEvent: StateFlow<Boolean> = _logoutEvent.asStateFlow()

    init {
        loadUserData()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            val userResult = userRepository.getCurrentUser()
            if (userResult.isSuccess) {
                _user.value = userResult.getOrNull()
                val incidentsResult = incidentRepository.getMyIncidents()
                if (incidentsResult.isSuccess) {
                    _incidents.value = incidentsResult.getOrNull() ?: emptyList()
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _user.value = null
            _incidents.value = emptyList()
            _logoutEvent.value = true
        }
    }

    fun getShortDescription(incident: IncidentResponse): String {
        return if (incident.description.length > 50) {
            incident.description.take(50) + "..."
        } else {
            incident.description
        }
    }

    fun formatDate(dateString: String): String {
        return try {
            dateString.split("T").first()
        } catch (e: Exception) {
            dateString
        }
    }
}
