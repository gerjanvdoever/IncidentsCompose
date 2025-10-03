package com.example.incidentscompose.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.incidentscompose.data.repository.AuthRepository
import com.example.incidentscompose.ui.states.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class LoginViewModel(
    private val repository: AuthRepository
) : BaseViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    private val _autoLoginState = MutableStateFlow<AutoLoginState>(AutoLoginState.Checking)
    val autoLoginState: StateFlow<AutoLoginState> = _autoLoginState.asStateFlow()

    fun login(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            _loginState.value = LoginState.Error("Please enter both username and password")
            return
        }

        _loginState.value = LoginState.Loading

        viewModelScope.launch {
            try {
                val success = withLoading {
                    repository.login(username, password)
                }
                _loginState.value = if (success) {
                    LoginState.Success
                } else {
                    LoginState.Error("Invalid username or password")
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
                _loginState.value = LoginState.Error(errorMessage)
            }
        }
    }

    fun checkAutoLogin() {
        viewModelScope.launch {
            try {
                val token = withLoading {
                    repository.getSavedToken()
                }
                _autoLoginState.value = if (!token.isNullOrEmpty()) {
                    AutoLoginState.TokenFound
                } else {
                    AutoLoginState.NoToken
                }
            } catch (e: Exception) {
                _autoLoginState.value = AutoLoginState.Error("Error checking saved login")
            }
        }
    }

    fun clearLoginState() {
        _loginState.value = LoginState.Idle
    }
}

sealed class LoginState {
    data object Idle : LoginState()
    data object Loading : LoginState()
    data object Success : LoginState()
    data class Error(val message: String) : LoginState()
}

sealed class AutoLoginState {
    data object Checking : AutoLoginState()
    data object TokenFound : AutoLoginState()
    data object NoToken : AutoLoginState()
    data class Error(val message: String) : AutoLoginState()
}