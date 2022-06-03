package com.habitrpg.wearos.habitica.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.habitrpg.wearos.habitica.data.repositories.TaskRepository
import com.habitrpg.wearos.habitica.data.repositories.UserRepository
import com.habitrpg.wearos.habitica.models.User
import com.habitrpg.wearos.habitica.util.ExceptionHandlerBuilder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    userRepository: UserRepository,
    val taskRepository: TaskRepository,
    exceptionBuilder: ExceptionHandlerBuilder
) : BaseViewModel(userRepository, exceptionBuilder) {
    var user: LiveData<User>

    init {
        user = userRepository.getUser().asLiveData()
        viewModelScope.launch {
            userRepository.retrieveUser()
            taskRepository.retrieveTasks()
        }
    }
}