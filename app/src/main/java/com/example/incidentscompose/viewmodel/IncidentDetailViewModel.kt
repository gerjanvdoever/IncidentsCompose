package com.example.incidentscompose.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.incidentscompose.data.model.*
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
import java.lang.Exception

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
                when (val result = incidentRepository.getIncidentById(incidentId)) {
                    is ApiResult.Success -> {
                        val incident = result.data
                        _currentIncident.value = incident
                        if (!incident.isAnonymous && incident.reportedBy != null) {
                            fetchReportedUser(incident.reportedBy)
                        }
                    }
                    is ApiResult.Unauthorized -> _unauthorizedState.value = true
                    is ApiResult.HttpError -> _toastMessage.value =
                        "Failed to load incident: ${result.message}"
                    is ApiResult.NetworkError -> _toastMessage.value =
                        "Network error: ${result.exception.message}"
                }
            }
        }
    }

    fun clearCurrentIncident() {
        _currentIncident.value = null
        clearReportedUser()
    }

    fun fetchReportedUser(userId: Long) {
        userFetchJob?.cancel()
        _userFetchTimeout.value = false

        userFetchJob = viewModelScope.launch {
            val timeoutJob = launch {
                delay(5000)
                if (_reportedUser.value == null) {
                    _userFetchTimeout.value = true
                }
            }

            try {
                withLoading {
                    when (val result = userRepository.getUserById(userId)) {
                        is ApiResult.Success -> {
                            _reportedUser.value = result.data
                            timeoutJob.cancel()
                        }
                        is ApiResult.Unauthorized -> _unauthorizedState.value = true
                        is ApiResult.HttpError -> _userFetchTimeout.value = true
                        is ApiResult.NetworkError -> _userFetchTimeout.value = true
                    }
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
                when (val result = incidentRepository.changeIncidentPriority(incidentId, priority)) {
                    is ApiResult.Success -> {
                        getIncidentById(incidentId)
                        _toastMessage.value = "Priority updated successfully"
                    }
                    is ApiResult.Unauthorized -> _unauthorizedState.value = true
                    is ApiResult.HttpError -> _toastMessage.value =
                        "Failed to update priority: ${result.message}"
                    is ApiResult.NetworkError -> _toastMessage.value =
                        "Network error: ${result.exception.message}"
                }
            }
        }
    }

    fun updateStatus(incidentId: Long, status: Status) {
        viewModelScope.launch {
            withLoading {
                when (val result = incidentRepository.changeIncidentStatus(incidentId, status)) {
                    is ApiResult.Success -> {
                        getIncidentById(incidentId)
                        _toastMessage.value = "Status updated successfully"
                    }
                    is ApiResult.Unauthorized -> _unauthorizedState.value = true
                    is ApiResult.HttpError -> _toastMessage.value =
                        "Failed to update status: ${result.message}"
                    is ApiResult.NetworkError -> _toastMessage.value =
                        "Network error: ${result.exception.message}"
                }
            }
        }
    }

    fun deleteIncident(incidentId: Long) {
        viewModelScope.launch {
            withLoading {
                when (val result = incidentRepository.deleteIncident(incidentId)) {
                    is ApiResult.Success -> _toastMessage.value = "Incident deleted successfully"
                    is ApiResult.Unauthorized -> _unauthorizedState.value = true
                    is ApiResult.HttpError -> _toastMessage.value =
                        "Failed to delete incident: ${result.message}"
                    is ApiResult.NetworkError -> _toastMessage.value =
                        "Network error: ${result.exception.message}"
                }
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
