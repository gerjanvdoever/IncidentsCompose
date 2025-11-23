package com.example.incidentscompose.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.incidentscompose.data.model.ApiResult
import com.example.incidentscompose.data.repository.AuthRepository
import com.example.incidentscompose.ui.states.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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
                val result = withLoading {
                    repository.login(username, password)
                }

                when (result) {
                    is ApiResult.Success -> _loginState.value = LoginState.Success

                    is ApiResult.HttpError -> _loginState.value =
                        LoginState.Error("Login failed")

                    is ApiResult.NetworkError -> _loginState.value =
                        LoginState.Error("Network error, Please check your internet connection or try again later")

                    is ApiResult.Timeout -> _loginState.value =
                        LoginState.Error("Request timed out. Please try again.")

                    is ApiResult.Unknown -> _loginState.value =
                        LoginState.Error("Unexpected error occurred. Please try again later.")

                    is ApiResult.Unauthorized -> _loginState.value =
                        LoginState.Error("Invalid username or password.")
                }
            } catch (e: Exception) {
                // purely a safety net for unexpected cases
                _loginState.value = LoginState.Error(
                    "Unexpected error: ${e.message ?: "Something went wrong"}"
                )
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
