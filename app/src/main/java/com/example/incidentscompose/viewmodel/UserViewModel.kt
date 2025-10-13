package com.example.incidentscompose.viewmodel

import androidx.lifecycle.viewModelScope
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

    fun updateProfile(
        username: String,
        email: String,
        newPassword: String?
    ) {
        viewModelScope.launch {
            try {
                withLoading {
                    _errorMessage.value = null

                    val updateRequest = UpdateUserRequest(
                        username = username,
                        email = email,
                        password = newPassword,
                        avatar = null
                    )

                    val result = userRepository.updateCurrentUser(updateRequest)

                    if (result.isSuccess) {
                        _updateSuccess.value = true
                    } else {
                        _errorMessage.value = result.exceptionOrNull()?.message ?: "Update failed"
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "An error occurred during update"
            }
        }
    }

    fun resetUpdateState() {
        _updateSuccess.value = false
        _errorMessage.value = null
    }
}