package com.habitrpg.wearos.habitica.ui.viewmodels

import androidx.lifecycle.SavedStateHandle
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.habitrpg.wearos.habitica.data.repositories.TaskRepository
import com.habitrpg.wearos.habitica.data.repositories.UserRepository
import com.habitrpg.wearos.habitica.managers.AppStateManager
import com.habitrpg.wearos.habitica.util.ExceptionHandlerBuilder
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ContinuePhoneViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        userRepository: UserRepository,
        taskRepository: TaskRepository,
        exceptionBuilder: ExceptionHandlerBuilder,
        appStateManager: AppStateManager,
    ) : BaseViewModel(userRepository, taskRepository, exceptionBuilder, appStateManager), MessageClient.OnMessageReceivedListener {
        val keepActive = savedStateHandle.get<Boolean>("keep_active") ?: false
        var onActionCompleted: (() -> Unit)? = null

        override fun onMessageReceived(event: MessageEvent) {
            when (event.path) {
                "/action_completed" -> onActionCompleted?.invoke()
            }
        }
    }
