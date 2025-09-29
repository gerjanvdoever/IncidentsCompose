// In your MyIncidentListViewModel
package com.example.incidentscompose.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.incidentscompose.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MyIncidentListViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _isLoggingOut = MutableStateFlow(false)
    val isLoggingOut: StateFlow<Boolean> = _isLoggingOut.asStateFlow()

    fun logout(onLogoutComplete: () -> Unit) {
        _isLoggingOut.value = true
        viewModelScope.launch {
            try {
                authRepository.logout()
                _isLoggingOut.value = false
                onLogoutComplete()
            } catch (e: Exception) {
                _isLoggingOut.value = false
                // Handle error if needed
                onLogoutComplete()
            }
        }
    }
}