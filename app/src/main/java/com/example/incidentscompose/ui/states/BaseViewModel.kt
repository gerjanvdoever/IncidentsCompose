package com.example.incidentscompose.ui.states

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

abstract class BaseViewModel : ViewModel() {
    private val _isBusy = MutableStateFlow(false)
    val isBusy: StateFlow<Boolean> = _isBusy.asStateFlow()

    private fun setBusy(busy: Boolean) {
        _isBusy.value = busy
    }

    protected suspend fun <T> withLoading(block: suspend () -> T): T {
        setBusy(true)
        return try {
            block()
        } finally {
            setBusy(false)
        }
    }
}