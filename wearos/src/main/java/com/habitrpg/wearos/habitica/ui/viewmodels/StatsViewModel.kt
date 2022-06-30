package com.habitrpg.wearos.habitica.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.habitrpg.wearos.habitica.data.repositories.TaskRepository
import com.habitrpg.wearos.habitica.data.repositories.UserRepository
import com.habitrpg.wearos.habitica.managers.LoadingManager
import com.habitrpg.wearos.habitica.models.user.User
import com.habitrpg.wearos.habitica.util.ExceptionHandlerBuilder
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StatsViewModel @Inject constructor(userRepository: UserRepository,
    taskRepository: TaskRepository,
    exceptionBuilder: ExceptionHandlerBuilder, loadingManager: LoadingManager
) : BaseViewModel(userRepository, taskRepository, exceptionBuilder, loadingManager) {

    var user: LiveData<User> = userRepository.getUser().asLiveData()

}