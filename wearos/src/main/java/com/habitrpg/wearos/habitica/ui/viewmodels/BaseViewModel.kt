package com.habitrpg.wearos.habitica.ui.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.habitrpg.wearos.habitica.data.repositories.UserRepository
import com.habitrpg.wearos.habitica.models.DisplayedError
import com.habitrpg.wearos.habitica.models.user.User
import com.habitrpg.wearos.habitica.util.ErrorPresenter
import com.habitrpg.wearos.habitica.util.ExceptionHandlerBuilder

open class BaseViewModel(
    val userRepository: UserRepository,
    val exceptionBuilder: ExceptionHandlerBuilder
): ViewModel(), ErrorPresenter {
    suspend fun retrieveUser(): User? {
        return userRepository.retrieveUser()
    }

    override val errorValues = MutableLiveData<DisplayedError>()
}