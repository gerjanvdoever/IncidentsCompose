package com.example.incidentscompose.viewmodel

import androidx.lifecycle.viewModelScope
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

    private val _unauthorizedState = MutableStateFlow(false)
    val unauthorizedState: StateFlow<Boolean> = _unauthorizedState.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    init{
        loadUserRole()
    }

    private fun loadUserRole(){
        viewModelScope.launch {
            _userRole.value = tokenPreferences.getUserRole()
        }
    }

    private fun getAllIncidents(){
        viewModelScope.launch {
            withLoading {
                try{
                    val result = incidentRepository.getAllIncidents()
                }
                catch (e: Exception){
                    if(e.message?.contains("Unauthorized", true) == true){
                        _unauthorizedState.value = true
                    }
                }
            }
        }
    }
}