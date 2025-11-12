package com.example.incidentscompose.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.incidentscompose.data.model.*
import com.example.incidentscompose.data.repository.UserRepository
import com.example.incidentscompose.data.store.TokenPreferences
import com.example.incidentscompose.ui.states.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserManagementViewModel(
    private val repository: UserRepository,
    private val tokenPreferences: TokenPreferences
) : BaseViewModel() {

    private val _userRole = MutableStateFlow<String?>(null)
    val userRole: StateFlow<String?> = _userRole.asStateFlow()

    private val _users = MutableStateFlow<List<UserResponse>>(emptyList())
    val users: StateFlow<List<UserResponse>> = _users.asStateFlow()

    private val _unauthorizedState = MutableStateFlow(false)
    val unauthorizedState: StateFlow<Boolean> = _unauthorizedState.asStateFlow()

    private val _showLoadMore = MutableStateFlow(false)
    val showLoadMore: StateFlow<Boolean> = _showLoadMore.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    private var currentPage = 0
    private val pageSize = 10

    init {
        loadUserRole()
        loadUsers()
    }

    private fun loadUserRole() {
        viewModelScope.launch {
            _userRole.value = tokenPreferences.getUserRole()
        }
    }

    fun loadUsers() {
        viewModelScope.launch {
            withLoading {
                try {
                    when (val result = repository.getAllUsers()) {
                        is ApiResult.Success -> {
                            _users.value = result.data
                            _showLoadMore.value = result.data.size >= pageSize
                        }
                        is ApiResult.HttpError -> _toastMessage.value =
                            "Failed to load users: ${result.message}"
                        is ApiResult.NetworkError -> _toastMessage.value =
                            "Network error: ${result.exception.message ?: "Please try again"}"
                        is ApiResult.Timeout -> _toastMessage.value = "Request timed out. Please try again."
                        is ApiResult.Unknown -> _toastMessage.value = "Unexpected error occurred."
                        is ApiResult.Unauthorized -> _unauthorizedState.value = true
                    }
                } catch (e: Exception) {
                    _toastMessage.value = "Unexpected error: ${e.message ?: "Please try again"}"
                }
            }
        }
    }

    fun loadMoreUsers() {
        viewModelScope.launch {
            withLoading {
                currentPage++
                try {
                    when (val result = repository.getAllUsers()) {
                        is ApiResult.Success -> {
                            _users.value += result.data
                            _showLoadMore.value = result.data.size >= pageSize
                        }
                        is ApiResult.HttpError -> {
                            currentPage--
                            _toastMessage.value = "Failed to load more users: ${result.message}"
                        }
                        is ApiResult.NetworkError -> {
                            currentPage--
                            _toastMessage.value =
                                "Network error: ${result.exception.message ?: "Please try again"}"
                        }
                        is ApiResult.Timeout -> {
                            currentPage--
                            _toastMessage.value = "Request timed out. Please try again."
                        }
                        is ApiResult.Unknown -> {
                            currentPage--
                            _toastMessage.value = "Unexpected error occurred."
                        }
                        is ApiResult.Unauthorized -> {
                            currentPage--
                            _unauthorizedState.value = true
                        }
                    }
                } catch (e: Exception) {
                    currentPage--
                    _toastMessage.value = "Unexpected error: ${e.message ?: "Please try again"}"
                }
            }
        }
    }

    fun changeUserRole(userId: Long, newRole: String) {
        viewModelScope.launch {
            withLoading {
                try {
                    when (val result = repository.updateUserRole(userId, newRole)) {
                        is ApiResult.Success -> {
                            _users.value = _users.value.map { user ->
                                if (user.id == userId.toString()) user.copy(role = newRole) else user
                            }
                            _toastMessage.value = "Role updated successfully"
                        }
                        is ApiResult.HttpError -> _toastMessage.value =
                            "Failed to update role: ${result.message}"
                        is ApiResult.NetworkError -> _toastMessage.value =
                            "Network error: ${result.exception.message ?: "Please try again"}"
                        is ApiResult.Timeout -> _toastMessage.value = "Request timed out. Please try again."
                        is ApiResult.Unknown -> _toastMessage.value = "Unexpected error occurred."
                        is ApiResult.Unauthorized -> _unauthorizedState.value = true
                    }
                } catch (e: Exception) {
                    _toastMessage.value = "Unexpected error: ${e.message ?: "Please try again"}"
                }
            }
        }
    }

    fun deleteUser(userId: Long) {
        viewModelScope.launch {
            withLoading {
                try {
                    when (val result = repository.deleteUser(userId)) {
                        is ApiResult.Success -> {
                            _users.value = _users.value.filter { it.id != userId.toString() }
                            _toastMessage.value = "User deleted successfully"
                        }
                        is ApiResult.HttpError -> _toastMessage.value =
                            "Failed to delete user: ${result.message}"
                        is ApiResult.NetworkError -> _toastMessage.value =
                            "Network error: ${result.exception.message ?: "Please try again"}"
                        is ApiResult.Timeout -> _toastMessage.value = "Request timed out. Please try again."
                        is ApiResult.Unknown -> _toastMessage.value = "Unexpected error occurred."
                        is ApiResult.Unauthorized -> _unauthorizedState.value = true
                    }
                } catch (e: Exception) {
                    _toastMessage.value = "Unexpected error: ${e.message ?: "Please try again"}"
                }
            }
        }
    }

    fun clearToastMessage() {
        _toastMessage.value = null
    }
}
