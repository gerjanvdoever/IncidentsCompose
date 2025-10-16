package com.example.incidentscompose.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.incidentscompose.data.model.UserResponse
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
): BaseViewModel() {

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
        loadUsers()
        loadUserRole()
    }

    private fun loadUserRole(){
        viewModelScope.launch {
            _userRole.value = tokenPreferences.getUserRole()
        }
    }

    private fun loadUsers(){
        viewModelScope.launch {
            withLoading {
                val result = repository.getAllUsers()
                result.fold(
                    onSuccess = { users ->
                        _users.value = users
                        _showLoadMore.value = users.size >= pageSize
                    },
                    onFailure = { exception ->
                        if (exception.message?.contains("Unauthorized", true) == true) {
                            _unauthorizedState.value = true
                        }
                        _toastMessage.value = "Failed to load users: ${exception.message}"
                    }
                )
            }
        }
    }

    fun loadMoreUsers(){
        viewModelScope.launch {
            withLoading {
                currentPage++
                val result = repository.getAllUsers()
                result.fold(
                    onSuccess = { newUsers ->
                        val currentUsers = _users.value
                        _users.value = currentUsers + newUsers
                        _showLoadMore.value = newUsers.size >= pageSize
                    },
                    onFailure = { exception ->
                        currentPage-- // Revert page on failure
                        if (exception.message?.contains("Unauthorized", true) == true) {
                            _unauthorizedState.value = true
                        }
                        _toastMessage.value = "Failed to load more users: ${exception.message}"
                    }
                )
            }
        }
    }

    fun changeUserRole(userId: Long, newRole: String){
        viewModelScope.launch {
            withLoading {
                val result = repository.updateUserRole(userId, newRole)
                result.fold(
                    onSuccess = {
                        val updatedUsers = _users.value.map { user ->
                            if (user.id == userId.toString()) {
                                user.copy(role = newRole)
                            } else {
                                user
                            }
                        }
                        _users.value = updatedUsers
                        _toastMessage.value = "Role updated successfully"
                    },
                    onFailure = { exception ->
                        if (exception.message?.contains("Unauthorized", true) == true) {
                            _unauthorizedState.value = true
                        }
                        _toastMessage.value = "Failed to update role: ${exception.message}"
                    }
                )
            }
        }
    }

    fun deleteUser(userId: Long) {
        viewModelScope.launch {
            withLoading {
                val result = repository.deleteUser(userId)
                result.fold(
                    onSuccess = { success ->
                        if (success) {
                            // Remove the user from the list
                            val updatedUsers = _users.value.filter { user ->
                                user.id != userId.toString()
                            }
                            _users.value = updatedUsers
                            _toastMessage.value = "User deleted successfully"
                        } else {
                            _toastMessage.value = "Failed to delete user"
                        }
                    },
                    onFailure = { exception ->
                        if (exception.message?.contains("Unauthorized", true) == true) {
                            _unauthorizedState.value = true
                        }
                        _toastMessage.value = "Failed to delete user: ${exception.message}"
                    }
                )
            }
        }
    }

    fun clearToastMessage() {
        _toastMessage.value = null
    }
}