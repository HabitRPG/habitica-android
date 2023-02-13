package com.habitrpg.wearos.habitica.managers

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppStateManager @Inject constructor() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    val isAppConnected = MutableStateFlow(true)

    fun startLoading() {
        _isLoading.value = true
    }

    fun endLoading() {
        _isLoading.value = false
    }
}
