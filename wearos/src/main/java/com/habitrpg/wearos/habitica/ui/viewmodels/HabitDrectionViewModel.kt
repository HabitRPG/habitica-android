package com.habitrpg.wearos.habitica.ui.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.habitrpg.common.habitica.models.responses.TaskDirection
import com.habitrpg.common.habitica.models.responses.TaskScoringResult
import com.habitrpg.wearos.habitica.data.repositories.TaskRepository
import com.habitrpg.wearos.habitica.data.repositories.UserRepository
import com.habitrpg.wearos.habitica.managers.LoadingManager
import com.habitrpg.wearos.habitica.util.ExceptionHandlerBuilder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HabitDrectionViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    userRepository: UserRepository,
    taskRepository: TaskRepository,
    exceptionBuilder: ExceptionHandlerBuilder, loadingManager: LoadingManager
) : BaseViewModel(userRepository, taskRepository, exceptionBuilder, loadingManager) {
    val taskID = savedStateHandle.get<String>("task_id")
    val task = taskRepository.getTask(taskID).asLiveData()

    val user = userRepository.getUser().asLiveData()

    fun scoreTask(direction: TaskDirection, onResult: (TaskScoringResult?) -> Unit) {
        viewModelScope.launch(exceptionBuilder.userFacing(this)) {
            val result = task.value?.let { taskRepository.scoreTask(user.value, it, direction) }
            onResult(result)
        }
    }
}
