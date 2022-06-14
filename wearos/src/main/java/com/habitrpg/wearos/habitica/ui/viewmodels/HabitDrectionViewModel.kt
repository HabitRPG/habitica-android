package com.habitrpg.wearos.habitica.ui.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.habitrpg.common.habitica.models.responses.TaskDirection
import com.habitrpg.wearos.habitica.data.repositories.TaskRepository
import com.habitrpg.wearos.habitica.data.repositories.UserRepository
import com.habitrpg.wearos.habitica.util.ExceptionHandlerBuilder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HabitDrectionViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    userRepository: UserRepository,
    val taskRepository: TaskRepository,
    exceptionBuilder: ExceptionHandlerBuilder
) : BaseViewModel(userRepository, exceptionBuilder) {
    val taskID = savedStateHandle.get<String>("task_id")
    val task = taskRepository.getTask(taskID).asLiveData()

    fun scoreTask(direction: TaskDirection) {
        viewModelScope.launch(exceptionBuilder.userFacing(this)) {
            task.value?.let { taskRepository.scoreTask(it, direction) }
        }
    }
}
