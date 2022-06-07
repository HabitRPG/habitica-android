package com.habitrpg.wearos.habitica.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.habitrpg.common.habitica.models.responses.TaskDirection
import com.habitrpg.common.habitica.models.responses.TaskScoringResult
import com.habitrpg.common.habitica.models.tasks.TaskType
import com.habitrpg.wearos.habitica.data.repositories.TaskRepository
import com.habitrpg.wearos.habitica.data.repositories.UserRepository
import com.habitrpg.wearos.habitica.models.tasks.Task
import com.habitrpg.wearos.habitica.util.ExceptionHandlerBuilder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val taskRepository: TaskRepository,
    userRepository: UserRepository,
    exceptionBuilder: ExceptionHandlerBuilder
) : BaseViewModel(userRepository, exceptionBuilder) {
    fun scoreTask(task: Task, direction: TaskDirection, onResult: (TaskScoringResult?) -> Unit) {
        viewModelScope.launch(exceptionBuilder.userFacing(this)) {
            val result = taskRepository.scoreTask(task, direction)
            onResult(result)
        }
    }

    var tasks: LiveData<List<Task>>
    val taskType = TaskType.from(savedStateHandle.get<String>("task_type"))

    init {
        tasks = taskRepository.getTasks(taskType ?: TaskType.HABIT).asLiveData()
    }
}