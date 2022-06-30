package com.habitrpg.wearos.habitica.ui.viewmodels

import androidx.lifecycle.asLiveData
import com.habitrpg.wearos.habitica.data.repositories.TaskRepository
import com.habitrpg.wearos.habitica.data.repositories.UserRepository
import com.habitrpg.wearos.habitica.managers.LoadingManager
import com.habitrpg.wearos.habitica.util.ExceptionHandlerBuilder
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    userRepository: UserRepository,
    taskRepository: TaskRepository,
    exceptionBuilder: ExceptionHandlerBuilder, loadingManager: LoadingManager
) : BaseViewModel(userRepository, taskRepository, exceptionBuilder, loadingManager) {
    val taskCounts = taskRepository.getActiveTaskCounts().asLiveData()
    val user = userRepository.getUser().asLiveData()
}