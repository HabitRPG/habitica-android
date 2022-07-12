package com.habitrpg.wearos.habitica.ui.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.habitrpg.common.habitica.models.responses.TaskDirection
import com.habitrpg.common.habitica.models.responses.TaskScoringResult
import com.habitrpg.common.habitica.models.tasks.TaskType
import com.habitrpg.wearos.habitica.data.repositories.TaskRepository
import com.habitrpg.wearos.habitica.data.repositories.UserRepository
import com.habitrpg.wearos.habitica.managers.AppStateManager
import com.habitrpg.wearos.habitica.models.tasks.Task
import com.habitrpg.wearos.habitica.util.ExceptionHandlerBuilder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    taskRepository: TaskRepository,
    userRepository: UserRepository,
    exceptionBuilder: ExceptionHandlerBuilder, appStateManager: AppStateManager
) : BaseViewModel(userRepository, taskRepository, exceptionBuilder, appStateManager) {
    val taskType = TaskType.from(savedStateHandle.get<String>("task_type"))
    val taskCount = MutableLiveData(0)
    val tasks = taskRepository.getTasks(taskType ?: TaskType.HABIT)
        .map {
            if (taskType == TaskType.DAILY || taskType == TaskType.TODO) {
                val taskList: MutableList<Any> = it.filter { it.isDue == true || it.type == TaskType.TODO }.sortedBy { it.completed }.toMutableList()
                val firstCompletedIndex = taskList.indexOfFirst { it is Task &&  it.completed }
                if (firstCompletedIndex >= 0) {
                    // since this is the index of the first completed task, this is also the number of incomplete tasks
                    taskCount.value = firstCompletedIndex
                    taskList.add(firstCompletedIndex, "Done today")
                } else {
                    taskCount.value = taskList.size
                }
                taskList
            } else {
                taskCount.value = it.size
                it
            }
        }
        .asLiveData()
    val user = userRepository.getUser()
        .asLiveData()

    fun scoreTask(task: Task, direction: TaskDirection, onResult: (TaskScoringResult?) -> Unit) {
        viewModelScope.launch(exceptionBuilder.userFacing(this)) {
            val result = taskRepository.scoreTask(
                userRepository.getUser().first(),
                task,
                direction
            )
            onResult(result)
        }
    }
}