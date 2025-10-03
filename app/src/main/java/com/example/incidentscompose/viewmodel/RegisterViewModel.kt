package com.example.incidentscompose.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.incidentscompose.data.repository.UserRepository
import com.example.incidentscompose.ui.states.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class RegisterViewModel(
    private val userRepository: UserRepository
) : BaseViewModel() {

    private val _registerState = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val registerState: StateFlow<RegisterState> = _registerState.asStateFlow()

    fun register(username: String, password: String, email: String, confirmPassword: String) {

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

        _registerState.value = RegisterState.Loading

        viewModelScope.launch {
            try {
                val success = withLoading {
                    userRepository.register(username, password, email, null)
                }
                _registerState.value = if (success) {
                    RegisterState.Success
                } else {
                    RegisterState.Error("Registration failed.")
                }
            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is ConnectException, is SocketTimeoutException, is UnknownHostException -> {
                        "Network error: Unable to connect to server."
                    }
                    else -> {
                        "Network error: ${e.message ?: "Please try again later"}"
                    }
                }
                _registerState.value = RegisterState.Error(errorMessage)
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