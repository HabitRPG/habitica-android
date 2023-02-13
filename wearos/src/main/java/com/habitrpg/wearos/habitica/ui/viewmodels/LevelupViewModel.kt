package com.habitrpg.wearos.habitica.ui.viewmodels

import androidx.lifecycle.asLiveData
import com.habitrpg.wearos.habitica.data.repositories.TaskRepository
import com.habitrpg.wearos.habitica.data.repositories.UserRepository
import com.habitrpg.wearos.habitica.managers.AppStateManager
import com.habitrpg.wearos.habitica.util.ExceptionHandlerBuilder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class LevelupViewModel @Inject constructor(
    userRepository: UserRepository,
    taskRepository: TaskRepository,
    exceptionBuilder: ExceptionHandlerBuilder,
    appStateManager: AppStateManager
) : BaseViewModel(userRepository, taskRepository, exceptionBuilder, appStateManager) {
    val level = userRepository.getUser()
        .map { it?.stats?.lvl }
        .asLiveData()
}
