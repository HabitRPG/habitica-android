package com.habitrpg.wearos.habitica.ui.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.habitrpg.wearos.habitica.data.repositories.TaskRepository
import com.habitrpg.wearos.habitica.data.repositories.UserRepository
import com.habitrpg.wearos.habitica.models.tasks.Task
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskListViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    userRepository: UserRepository
) : BaseViewModel(userRepository) {
    val tasks = MutableLiveData<List<Task>>()

    init {
        viewModelScope.launch {
            tasks.value = taskRepository.retrieveTasks()?.tasks?.values?.toList()
        }
    }
}