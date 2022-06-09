package com.habitrpg.wearos.habitica.ui.viewmodels

import android.content.SharedPreferences
import androidx.core.content.edit
import com.habitrpg.wearos.habitica.data.ApiClient
import com.habitrpg.wearos.habitica.data.repositories.UserRepository
import com.habitrpg.wearos.habitica.util.ExceptionHandlerBuilder
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(userRepository: UserRepository,
    exceptionBuilder: ExceptionHandlerBuilder,
    private val apiClient: ApiClient,
    private val sharedPreferences: SharedPreferences
) : BaseViewModel(userRepository, exceptionBuilder) {

    fun logout() {
        sharedPreferences.edit {
            clear()
        }
        apiClient.updateAuthenticationCredentials(null, null)
    }
}
