package com.habitrpg.wearos.habitica.ui.viewmodels

import androidx.lifecycle.SavedStateHandle
import com.habitrpg.common.habitica.models.responses.TaskScoringResult
import com.habitrpg.wearos.habitica.data.repositories.UserRepository
import com.habitrpg.wearos.habitica.util.ExceptionHandlerBuilder
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TaskResultViewModel @Inject constructor(savedStateHandle: SavedStateHandle, userRepository: UserRepository,
    exceptionBuilder: ExceptionHandlerBuilder
) : BaseViewModel(userRepository, exceptionBuilder) {
    val hasDrop: Boolean
    get() {
        return result?.drop?.key?.isNotBlank() == true || (result?.questItemsFound ?: 0) > 0
    }
    val result = savedStateHandle.get<TaskScoringResult>("result")
}
