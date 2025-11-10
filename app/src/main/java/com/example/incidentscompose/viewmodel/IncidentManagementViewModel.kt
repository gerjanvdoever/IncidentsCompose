package com.example.incidentscompose.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.incidentscompose.data.model.IncidentResponse
import com.example.incidentscompose.data.repository.IncidentRepository
import com.example.incidentscompose.data.store.TokenPreferences
import com.example.incidentscompose.ui.states.BaseViewModel
import com.example.incidentscompose.util.IncidentFilterHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class IncidentManagementViewModel(
    private val incidentRepository: IncidentRepository,
    private val tokenPreferences: TokenPreferences
) : BaseViewModel() {

    private val _userRole = MutableStateFlow<String?>(null)
    val userRole: StateFlow<String?> = _userRole.asStateFlow()

    private val _allIncidents = MutableStateFlow<List<IncidentResponse>>(emptyList())
    val allIncidents: StateFlow<List<IncidentResponse>> = _allIncidents.asStateFlow()

    private val _displayedIncidents = MutableStateFlow<List<IncidentResponse>>(emptyList())
    val displayedIncidents: StateFlow<List<IncidentResponse>> = _displayedIncidents.asStateFlow()

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

    private var currentDisplayCount = 0
    private val pageSize = 20

    init {
        loadUserRole()
        getAllIncidents()
    }

    private fun loadUserRole() {
        viewModelScope.launch {
            _userRole.value = tokenPreferences.getUserRole()
        }
    }

    fun getAllIncidents() {
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

    fun loadMoreIncidents() {
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

    fun refreshIncidents() {
        currentDisplayCount = 0
        getAllIncidents()
    }

    fun clearToastMessage() {
        _toastMessage.value = null
    }
}