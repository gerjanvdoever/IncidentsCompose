package com.example.incidentscompose.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.incidentscompose.data.model.IncidentResponse
import com.example.incidentscompose.data.model.Priority
import com.example.incidentscompose.data.model.Status
import com.example.incidentscompose.data.model.UserResponse
import com.example.incidentscompose.data.repository.IncidentRepository
import com.example.incidentscompose.data.repository.UserRepository
import com.example.incidentscompose.data.store.TokenPreferences
import com.example.incidentscompose.ui.states.BaseViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class IncidentDetailViewModel(
    private val incidentRepository: IncidentRepository,
    private val tokenPreferences: TokenPreferences,
    private val userRepository: UserRepository
) : BaseViewModel() {

    private val _userRole = MutableStateFlow<String?>(null)

    private val _currentIncident = MutableStateFlow<IncidentResponse?>(null)
    val currentIncident: StateFlow<IncidentResponse?> = _currentIncident.asStateFlow()

    private val _reportedUser = MutableStateFlow<UserResponse?>(null)
    val reportedUser: StateFlow<UserResponse?> = _reportedUser.asStateFlow()

    private val _userFetchTimeout = MutableStateFlow(false)
    val userFetchTimeout: StateFlow<Boolean> = _userFetchTimeout.asStateFlow()

    private val _unauthorizedState = MutableStateFlow(false)
    val unauthorizedState: StateFlow<Boolean> = _unauthorizedState.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    private var userFetchJob: Job? = null

    init {
        loadUserRole()
    }

    private fun loadUserRole() {
        viewModelScope.launch {
            _userRole.value = tokenPreferences.getUserRole()
        }
    }

    fun getIncidentById(incidentId: Long) {
        viewModelScope.launch {
            withLoading {
                val result = incidentRepository.getIncidentById(incidentId)
                result.fold(
                    onSuccess = { incident ->
                        _currentIncident.value = incident
                        if (!incident.isAnonymous && incident.reportedBy != null) {
                            fetchReportedUser(incident.reportedBy)
                        }
                    },
                    onFailure = { exception ->
                        if (exception.message?.contains("Unauthorized", true) == true) {
                            _unauthorizedState.value = true
                        } else {
                            _toastMessage.value = "Failed to load incident: ${exception.message}"
                        }
                    }
                )
            }
        }
    }

    fun clearCurrentIncident() {
        _currentIncident.value = null
        clearReportedUser()
    }

    fun fetchReportedUser(userId: Long) {
        // Cancel any existing fetch job
        userFetchJob?.cancel()

        // Reset timeout state
        _userFetchTimeout.value = false

        userFetchJob = viewModelScope.launch {
            // Start timeout timer
            val timeoutJob = launch {
                delay(5000) // 5 seconds timeout
                if (_reportedUser.value == null) {
                    _userFetchTimeout.value = true
                }
            }

            try {
                withLoading {
                    val result = userRepository.getUserById(userId)
                    result.fold(
                        onSuccess = { user ->
                            _reportedUser.value = user
                            timeoutJob.cancel() // Cancel timeout if successful
                        },
                        onFailure = { exception ->
                            timeoutJob.cancel() // Cancel timeout
                            if (exception.message?.contains("Unauthorized", true) == true) {
                                _unauthorizedState.value = true
                            } else {
                                _userFetchTimeout.value = true
                            }
                        }
                    )
                }
            } catch (e: Exception) {
                timeoutJob.cancel()
                _userFetchTimeout.value = true
            }
        }
    }

    fun clearReportedUser() {
        userFetchJob?.cancel()
        _reportedUser.value = null
        _userFetchTimeout.value = false
    }

    fun updatePriority(incidentId: Long, priority: Priority) {
        viewModelScope.launch {
            withLoading {
                val result = incidentRepository.changeIncidentPriority(incidentId, priority)
                result.fold(
                    onSuccess = {
                        // Force refresh the incident data from API
                        getIncidentById(incidentId)
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
                    onSuccess = {
                        // Force refresh the incident data from API
                        getIncidentById(incidentId)
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

    fun clearToastMessage() {
        _toastMessage.value = null
    }

    override fun onCleared() {
        super.onCleared()
        userFetchJob?.cancel()
    }
}