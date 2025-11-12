package com.example.incidentscompose.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.incidentscompose.data.model.IncidentResponse
import com.example.incidentscompose.data.model.UserResponse
import com.example.incidentscompose.data.model.ApiResult
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

class MyIncidentListViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val incidentRepository: IncidentRepository,
    private val tokenPreferences: TokenPreferences,
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

    private val _userRole = MutableStateFlow<String?>(null)
    val userRole: StateFlow<String?> = _userRole.asStateFlow()

    private val _unauthorizedState = MutableStateFlow(false)
    val unauthorizedState: StateFlow<Boolean> = _unauthorizedState.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    init {
        loadUserRole()
        loadUserData()
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
                when (val userResult = userRepository.getCurrentUser()) {
                    is ApiResult.Success -> {
                        _user.value = userResult.data
                        loadUserIncidents()
                    }

                    is ApiResult.HttpError -> handleUserError(userResult)
                    is ApiResult.NetworkError -> handleUserError(userResult)
                    is ApiResult.Timeout -> {
                        _toastMessage.value = "Fetching user data timed out."
                        _user.value = null
                    }
                    is ApiResult.Unknown -> {
                        _toastMessage.value = "Unexpected error fetching user data."
                        _user.value = null
                    }
                    is ApiResult.Unauthorized -> logout()
                }
            } catch (e: Exception) {
                // Safety net
                _toastMessage.value = "Unexpected error: ${e.message ?: "Please try again"}"
                logout()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadUserIncidents() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                when (val incidentsResult = incidentRepository.getMyIncidents()) {
                    is ApiResult.Success -> _incidents.value = incidentsResult.data

                    is ApiResult.HttpError -> handleIncidentError(incidentsResult)
                    is ApiResult.NetworkError -> handleIncidentError(incidentsResult)
                    is ApiResult.Timeout -> _toastMessage.value = "Fetching incidents timed out."
                    is ApiResult.Unknown -> _toastMessage.value = "Unexpected error fetching incidents."
                    is ApiResult.Unauthorized -> logout()
                }
            } catch (e: Exception) {
                _toastMessage.value = "Unexpected error: ${e.message ?: "Please try again"}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun handleUserError(result: ApiResult<*>): Boolean {
        if (result is ApiResult.HttpError && result.code == 401) {
            _unauthorizedState.value = true
        }
        logout()
        return false
    }

    private fun handleIncidentError(result: ApiResult<*>): Boolean {
        if (result is ApiResult.HttpError && result.code == 401) {
            _unauthorizedState.value = true
        }
        // Leave incidents list unchanged
        return false
    }

    fun logout() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                authRepository.logout()
            } finally {
                _user.value = null
                _incidents.value = emptyList()
                _logoutEvent.value = true
                _isLoading.value = false
            }
        }
    }

    fun resetLogoutEvent() {
        _logoutEvent.value = false
    }

    fun saveSelectedIncident(incident: IncidentResponse) {
        viewModelScope.launch {
            incidentDataStore.saveSelectedIncident(incident)
        }
    }

    fun refreshIncidents() {
        loadUserIncidents()
    }

    fun clearToastMessage() {
        _toastMessage.value = null
    }
}
