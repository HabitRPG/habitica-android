package com.habitrpg.wearos.habitica.ui.viewmodels

import com.habitrpg.common.habitica.models.tasks.Frequency
import com.habitrpg.common.habitica.models.tasks.TaskType
import com.habitrpg.wearos.habitica.data.repositories.TaskRepository
import com.habitrpg.wearos.habitica.data.repositories.UserRepository
import com.habitrpg.wearos.habitica.managers.LoadingManager
import com.habitrpg.wearos.habitica.models.tasks.Task
import com.habitrpg.wearos.habitica.util.ExceptionHandlerBuilder
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TaskFormViewModel @Inject constructor(
    userRepository: UserRepository,
    val taskRepository: TaskRepository,
    exceptionBuilder: ExceptionHandlerBuilder, loadingManager: LoadingManager
) : BaseViewModel(userRepository, exceptionBuilder, loadingManager) {
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