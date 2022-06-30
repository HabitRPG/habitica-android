package com.habitrpg.wearos.habitica.ui.viewmodels

import androidx.lifecycle.SavedStateHandle
import com.habitrpg.common.habitica.models.responses.TaskScoringResult
import com.habitrpg.wearos.habitica.data.repositories.TaskRepository
import com.habitrpg.wearos.habitica.data.repositories.UserRepository
import com.habitrpg.wearos.habitica.managers.LoadingManager
import com.habitrpg.wearos.habitica.util.ExceptionHandlerBuilder
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TaskResultViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    taskRepository: TaskRepository,
    userRepository: UserRepository,
    exceptionBuilder: ExceptionHandlerBuilder, loadingManager: LoadingManager
) : BaseViewModel(userRepository, taskRepository, exceptionBuilder, loadingManager) {
    val hasDrop: Boolean
        get() {
            return result?.drop?.key?.isNotBlank() == true // || (result?.questItemsFound ?: 0) > 0
        }
    val result = savedStateHandle.get<TaskScoringResult>("result")
}
