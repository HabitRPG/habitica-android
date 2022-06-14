package com.habitrpg.wearos.habitica.ui.viewmodels

import androidx.lifecycle.SavedStateHandle
import com.habitrpg.common.habitica.models.tasks.Frequency
import com.habitrpg.common.habitica.models.tasks.TaskType
import com.habitrpg.wearos.habitica.data.repositories.TaskRepository
import com.habitrpg.wearos.habitica.data.repositories.UserRepository
import com.habitrpg.wearos.habitica.models.tasks.Task
import com.habitrpg.wearos.habitica.util.ExceptionHandlerBuilder
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TaskFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    userRepository: UserRepository,
    val taskRepository: TaskRepository,
    exceptionBuilder: ExceptionHandlerBuilder
) : BaseViewModel(userRepository, exceptionBuilder) {
    suspend fun saveTask(text: CharSequence?, taskType: TaskType?) {
        if (text?.isNotBlank() != true || taskType == null) {
            return
        }
        val task = Task()
        task.text = text.toString()
        task.type = taskType
        task.priority = 1.0f
        task.up = true
        task.everyX = 1
        task.frequency = Frequency.DAILY
        taskRepository.createTask(task)
    }
}