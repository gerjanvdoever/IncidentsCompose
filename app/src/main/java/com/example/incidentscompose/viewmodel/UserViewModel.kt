package com.example.incidentscompose.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.incidentscompose.data.model.ApiResult
import com.example.incidentscompose.data.model.UpdateUserRequest
import com.example.incidentscompose.data.repository.UserRepository
import com.example.incidentscompose.ui.states.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserViewModel(
    private val userRepository: UserRepository
) : BaseViewModel() {

    private val _updateSuccess = MutableStateFlow(false)
    val updateSuccess: StateFlow<Boolean> = _updateSuccess.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _unauthorizedState = MutableStateFlow(false)
    val unauthorizedState: StateFlow<Boolean> = _unauthorizedState.asStateFlow()

    fun updateProfile(
        username: String,
        email: String,
        newPassword: String?
    ) {
        viewModelScope.launch {
            withLoading {
                _errorMessage.value = null
                _updateSuccess.value = false

                val updateRequest = UpdateUserRequest(
                    username = username,
                    email = email,
                    password = newPassword,
                    avatar = null
                )

                when (val result = userRepository.updateCurrentUser(updateRequest)) {
                    is ApiResult.Success -> _updateSuccess.value = true
                    is ApiResult.HttpError -> _errorMessage.value = "Update failed: ${result.message}"
                    is ApiResult.NetworkError -> _errorMessage.value = "Network error: ${result.exception.message}"
                    is ApiResult.Unauthorized -> _unauthorizedState.value = true
                }
            }
        }
    }

    fun resetUpdateState() {
        _updateSuccess.value = false
        _errorMessage.value = null
        _unauthorizedState.value = false
    }
}
