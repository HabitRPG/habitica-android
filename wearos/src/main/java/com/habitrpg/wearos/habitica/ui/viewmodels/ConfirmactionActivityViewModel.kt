package com.habitrpg.wearos.habitica.ui.viewmodels

import androidx.lifecycle.SavedStateHandle
import com.habitrpg.android.habitica.R
import com.habitrpg.wearos.habitica.data.repositories.TaskRepository
import com.habitrpg.wearos.habitica.data.repositories.UserRepository
import com.habitrpg.wearos.habitica.managers.AppStateManager
import com.habitrpg.wearos.habitica.util.ExceptionHandlerBuilder
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ConfirmactionActivityViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    userRepository: UserRepository,
    taskRepository: TaskRepository,
    exceptionBuilder: ExceptionHandlerBuilder,
    appStateManager: AppStateManager
) : BaseViewModel(userRepository, taskRepository, exceptionBuilder, appStateManager) {
    val icon: Int = savedStateHandle["icon"] ?: R.drawable.error
    val text: String? = savedStateHandle["text"]
}
