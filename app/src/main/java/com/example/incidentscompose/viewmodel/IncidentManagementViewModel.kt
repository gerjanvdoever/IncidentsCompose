package com.example.incidentscompose.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.incidentscompose.data.model.IncidentResponse
import com.example.incidentscompose.data.model.Priority
import com.example.incidentscompose.data.model.Status
import com.example.incidentscompose.data.model.UserResponse
import com.example.incidentscompose.data.repository.IncidentRepository
import com.example.incidentscompose.data.repository.UserRepository
import com.example.incidentscompose.data.store.IncidentDataStore
import com.example.incidentscompose.data.store.TokenPreferences
import com.example.incidentscompose.ui.states.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class IncidentManagementViewModel(
    private val incidentRepository: IncidentRepository,
    private val incidentDataStore: IncidentDataStore,
    private val tokenPreferences: TokenPreferences,
    private val userRepository: UserRepository
): BaseViewModel() {

    private val _userRole = MutableStateFlow<String?>(null)
    val userRole: StateFlow<String?> = _userRole.asStateFlow()

    private val _allIncidents = MutableStateFlow<List<IncidentResponse>>(emptyList())
    private val _displayedIncidents = MutableStateFlow<List<IncidentResponse>>(emptyList())
    val displayedIncidents: StateFlow<List<IncidentResponse>> = _displayedIncidents.asStateFlow()

    private val _reportedUser = MutableStateFlow<UserResponse?>(null)
    val reportedUser: StateFlow<UserResponse?> = _reportedUser.asStateFlow()

    private val _unauthorizedState = MutableStateFlow(false)
    val unauthorizedState: StateFlow<Boolean> = _unauthorizedState.asStateFlow()

    private val _showLoadMore = MutableStateFlow(false)
    val showLoadMore: StateFlow<Boolean> = _showLoadMore.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    private var currentDisplayCount = 0
    private val pageSize = 20

    init{
        loadUserRole()
        getAllIncidents()
    }

    private fun loadUserRole(){
        viewModelScope.launch {
            _userRole.value = tokenPreferences.getUserRole()
        }
    }

    private fun getAllIncidents(){
        viewModelScope.launch {
            withLoading {
                val result = incidentRepository.getAllIncidents()
                result.fold(
                    onSuccess = { incidents ->
                        _allIncidents.value = incidents
                        // Show first pageSize incidents initially
                        currentDisplayCount = minOf(pageSize, incidents.size)
                        _displayedIncidents.value = incidents.take(currentDisplayCount)
                        _showLoadMore.value = incidents.size > currentDisplayCount
                    },
                    onFailure = { exception ->
                        if (exception.message?.contains("Unauthorized", true) == true) {
                            _unauthorizedState.value = true
                        } else {
                            _toastMessage.value = exception.message
                        }
                    }
                )
            }
        }
    }

    fun loadMoreIncidents(){
        val allIncidents = _allIncidents.value
        val newDisplayCount = minOf(currentDisplayCount + pageSize, allIncidents.size)

        if (newDisplayCount > currentDisplayCount) {
            _displayedIncidents.value = allIncidents.take(newDisplayCount)
            currentDisplayCount = newDisplayCount
            _showLoadMore.value = allIncidents.size > currentDisplayCount
        } else {
            _showLoadMore.value = false
        }
    }

    fun fetchReportedUser(userId: Long) {
        viewModelScope.launch {
            withLoading {
                val result = userRepository.getUserById(userId)
                result.fold(
                    onSuccess = { user ->
                        _reportedUser.value = user
                    },
                    onFailure = { exception ->
                        if (exception.message?.contains("Unauthorized", true) == true) {
                            _unauthorizedState.value = true
                        } else {
                            _toastMessage.value = "Failed to load user info: ${exception.message}"
                        }
                    }
                )
            }
        }
    }

    fun clearReportedUser(){
        _reportedUser.value = null
    }

    fun updatePriority(incidentId: Long, priority: Priority) {
        viewModelScope.launch {
            withLoading {
                val result = incidentRepository.changeIncidentPriority(incidentId, priority)
                result.fold(
                    onSuccess = { updatedIncident ->
                        // Update the incident in both lists
                        _allIncidents.value = _allIncidents.value.map { incident ->
                            if (incident.id == incidentId) updatedIncident else incident
                        }
                        _displayedIncidents.value = _displayedIncidents.value.map { incident ->
                            if (incident.id == incidentId) updatedIncident else incident
                        }
                        _toastMessage.value = "Priority updated successfully"
                    },
                    onFailure = { exception ->
                        if (exception.message?.contains("Unauthorized", true) == true) {
                            _unauthorizedState.value = true
                        } else {
                            _toastMessage.value = "Failed to update priority: ${exception.message}"
                        }
                    }
                )
            }
        }
    }

    fun updateStatus(incidentId: Long, status: Status) {
        viewModelScope.launch {
            withLoading {
                val result = incidentRepository.changeIncidentStatus(incidentId, status)
                result.fold(
                    onSuccess = { updatedIncident ->
                        // Update the incident in both lists
                        _allIncidents.value = _allIncidents.value.map { incident ->
                            if (incident.id == incidentId) updatedIncident else incident
                        }
                        _displayedIncidents.value = _displayedIncidents.value.map { incident ->
                            if (incident.id == incidentId) updatedIncident else incident
                        }
                        _toastMessage.value = "Status updated successfully"
                    },
                    onFailure = { exception ->
                        if (exception.message?.contains("Unauthorized", true) == true) {
                            _unauthorizedState.value = true
                        } else {
                            _toastMessage.value = "Failed to update status: ${exception.message}"
                        }
                    }
                )
            }
        }
    }

    fun deleteIncident(incidentId: Long) {
        viewModelScope.launch {
            withLoading {
                val result = incidentRepository.deleteIncident(incidentId)
                result.fold(
                    onSuccess = {
                        // Remove the incident from both lists
                        _allIncidents.value = _allIncidents.value.filter { it.id != incidentId }
                        _displayedIncidents.value = _displayedIncidents.value.filter { it.id != incidentId }
                        _toastMessage.value = "Incident deleted successfully"
                    },
                    onFailure = { exception ->
                        if (exception.message?.contains("Unauthorized", true) == true) {
                            _unauthorizedState.value = true
                        } else {
                            _toastMessage.value = "Failed to delete incident: ${exception.message}"
                        }
                    }
                )
            }
        }
    }

    fun refreshIncidents() {
        currentDisplayCount = 0
        getAllIncidents()
    }

    fun clearToastMessage(){
        _toastMessage.value = null
    }

    fun saveSelectedIncident(incident: IncidentResponse) {
        viewModelScope.launch {
            incidentDataStore.saveSelectedIncident(incident)
        }
    }

    fun getSelectedIncident() = incidentDataStore.selectedIncident

    fun clearSelectedIncident(){
        viewModelScope.launch {
            incidentDataStore.clearSelectedIncident()
        }
    }

    fun onIncidentTap(incident: IncidentResponse) {
        saveSelectedIncident(incident)
        // If incident has a reported user, fetch their info
        incident.reportedBy?.let { userId ->
            fetchReportedUser(userId)
        }
    }
}