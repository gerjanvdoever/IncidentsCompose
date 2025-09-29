package com.example.incidentscompose.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.incidentscompose.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _isBusy = MutableStateFlow(false)
    val isBusy: StateFlow<Boolean> = _isBusy.asStateFlow()

    private val _registerState = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val registerState: StateFlow<RegisterState> = _registerState.asStateFlow()

    fun register(username: String, password: String, email: String, confirmPassword: String) {
        // Validation
        if (username.isBlank() || password.isBlank() || email.isBlank() || confirmPassword.isBlank()) {
            _registerState.value = RegisterState.Error("Please fill in all fields")
            return
        }

        if (password != confirmPassword) {
            _registerState.value = RegisterState.Error("Passwords do not match")
            return
        }

        if (password.length < 6) {
            _registerState.value = RegisterState.Error("Password must be at least 6 characters")
            return
        }

        if (!isValidEmail(email)) {
            _registerState.value = RegisterState.Error("Please enter a valid email address")
            return
        }

        _isBusy.value = true
        _registerState.value = RegisterState.Loading

        viewModelScope.launch {
            try {
                // null for now on avatar, implement it later
                val success = userRepository.register(username, password, email, null)
                _registerState.value = if (success) {
                    RegisterState.Success
                } else {
                    RegisterState.Error("Registration failed. Username or email may already exist.")
                }
            } catch (e: Exception) {
                _registerState.value = RegisterState.Error("Network error: ${e.message}")
            } finally {
                _isBusy.value = false
            }
        }
    }

    fun clearRegisterState() {
        _registerState.value = RegisterState.Idle
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}

sealed class RegisterState {
    data object Idle : RegisterState()
    data object Loading : RegisterState()
    data object Success : RegisterState()
    data class Error(val message: String) : RegisterState()
}