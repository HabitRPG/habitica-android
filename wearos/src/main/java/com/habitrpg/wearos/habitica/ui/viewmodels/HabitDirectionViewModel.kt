package com.habitrpg.wearos.habitica.ui.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.asLiveData
import com.habitrpg.wearos.habitica.data.repositories.TaskRepository
import com.habitrpg.wearos.habitica.data.repositories.UserRepository
import com.habitrpg.wearos.habitica.managers.AppStateManager
import com.habitrpg.wearos.habitica.util.ExceptionHandlerBuilder
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HabitDirectionViewModel
@Inject
constructor(
    savedStateHandle: SavedStateHandle,
    userRepository: UserRepository,
    taskRepository: TaskRepository,
    exceptionBuilder: ExceptionHandlerBuilder,
    appStateManager: AppStateManager
) : BaseViewModel(userRepository, taskRepository, exceptionBuilder, appStateManager) {
    val taskID = savedStateHandle.get<String>("task_id")
    val task = taskRepository.getTask(taskID).asLiveData()

    val user = userRepository.getUser().asLiveData()
}
