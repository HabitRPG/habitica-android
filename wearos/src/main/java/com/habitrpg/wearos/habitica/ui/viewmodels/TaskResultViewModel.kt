package com.habitrpg.wearos.habitica.ui.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.asLiveData
import com.habitrpg.shared.habitica.models.responses.TaskScoringResult
import com.habitrpg.wearos.habitica.data.repositories.TaskRepository
import com.habitrpg.wearos.habitica.data.repositories.UserRepository
import com.habitrpg.wearos.habitica.managers.AppStateManager
import com.habitrpg.wearos.habitica.util.ExceptionHandlerBuilder
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TaskResultViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    taskRepository: TaskRepository,
    userRepository: UserRepository,
    exceptionBuilder: ExceptionHandlerBuilder,
    appStateManager: AppStateManager
) : BaseViewModel(userRepository, taskRepository, exceptionBuilder, appStateManager) {
    val user = userRepository.getUser().asLiveData()
    val hasLeveledUp: Boolean
        get() = result?.hasLeveledUp == true
    val hasDied: Boolean
        get() = result?.hasDied == true
    val hasDrop: Boolean
        get() {
            return result?.drop?.key?.isNotBlank() == true // || (result?.questItemsFound ?: 0) > 0
        }
    val result = savedStateHandle.get<TaskScoringResult>("result")
}
