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
import com.example.incidentscompose.util.IncidentFilterHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class IncidentManagementViewModel(
    private val incidentRepository: IncidentRepository,
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

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedPriorityFilter = MutableStateFlow<Set<String>>(emptySet())
    val selectedPriorityFilter: StateFlow<Set<String>> = _selectedPriorityFilter.asStateFlow()

    private val _selectedStatusFilter = MutableStateFlow<Set<String>>(emptySet())
    val selectedStatusFilter: StateFlow<Set<String>> = _selectedStatusFilter.asStateFlow()

    private val _selectedCategoryFilter = MutableStateFlow<Set<String>>(emptySet())
    val selectedCategoryFilter: StateFlow<Set<String>> = _selectedCategoryFilter.asStateFlow()

    private val _filteredIncidents = MutableStateFlow<List<IncidentResponse>>(emptyList())
    val filteredIncidents: StateFlow<List<IncidentResponse>> = _filteredIncidents.asStateFlow()

    private val _currentIncident = MutableStateFlow<IncidentResponse?>(null)
    val currentIncident: StateFlow<IncidentResponse?> = _currentIncident.asStateFlow()

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
                        applyFilters() // Apply filters after loading
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

    fun loadMoreIncidents(){
        val allIncidents = _allIncidents.value
        val newDisplayCount = minOf(currentDisplayCount + pageSize, allIncidents.size)

        if (newDisplayCount > currentDisplayCount) {
            _displayedIncidents.value = allIncidents.take(newDisplayCount)
            currentDisplayCount = newDisplayCount
            _showLoadMore.value = allIncidents.size > currentDisplayCount
            applyFilters() // Apply filters after loading more
        } else {
            _showLoadMore.value = false
        }
    }

    fun clearCurrentIncident() {
        _currentIncident.value = null
        clearReportedUser()
    }

    // Filter methods
    private fun applyFilters() {
        val filtered = IncidentFilterHelper.instance.filterIncidents(
            incidents = _displayedIncidents.value,
            searchQuery = _searchQuery.value,
            priorityFilter = _selectedPriorityFilter.value,
            statusFilter = _selectedStatusFilter.value,
            categoryFilter = _selectedCategoryFilter.value
        )
        _filteredIncidents.value = filtered
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        applyFilters()
    }

    fun updatePriorityFilter(priorities: Set<String>) {
        _selectedPriorityFilter.value = priorities
        applyFilters()
    }

    fun updateStatusFilter(statuses: Set<String>) {
        _selectedStatusFilter.value = statuses
        applyFilters()
    }

    fun updateCategoryFilter(categories: Set<String>) {
        _selectedCategoryFilter.value = categories
        applyFilters()
    }

    fun clearAllFilters() {
        _searchQuery.value = ""
        _selectedPriorityFilter.value = emptySet()
        _selectedStatusFilter.value = emptySet()
        _selectedCategoryFilter.value = emptySet()
        applyFilters()
    }

    val hasActiveFilters: Boolean
        get() = IncidentFilterHelper.instance.hasActiveFilters(
            searchQuery = _searchQuery.value,
            priorityFilter = _selectedPriorityFilter.value,
            statusFilter = _selectedStatusFilter.value,
            categoryFilter = _selectedCategoryFilter.value
        )

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
                    onSuccess = {
                        // Force refresh the incident data from API
                        getIncidentById(incidentId)
                        _toastMessage.value = "Priority updated successfully"

                        // Also refresh the list to keep it in sync
                        refreshIncidentInList(incidentId)
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

                        // Also refresh the incident in the list
                        refreshIncidentInList(incidentId)
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

    private fun refreshIncidentInList(incidentId: Long) {
        viewModelScope.launch {
            val result = incidentRepository.getIncidentById(incidentId)
            result.fold(
                onSuccess = { refreshedIncident ->
                    _allIncidents.value = _allIncidents.value.map { incident ->
                        if (incident.id == incidentId) refreshedIncident else incident
                    }
                    _displayedIncidents.value = _displayedIncidents.value.map { incident ->
                        if (incident.id == incidentId) refreshedIncident else incident
                    }
                    applyFilters()
                },
                onFailure = {
                    // Silently fail for list refresh, main incident refresh will handle errors
                }
            )
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
                        applyFilters() // Reapply filters after deletion
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
}