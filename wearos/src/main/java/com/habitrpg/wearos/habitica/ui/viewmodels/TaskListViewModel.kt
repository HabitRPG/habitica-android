package com.habitrpg.wearos.habitica.ui.viewmodels

import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.habitrpg.shared.habitica.models.responses.TaskDirection
import com.habitrpg.shared.habitica.models.responses.TaskScoringResult
import com.habitrpg.shared.habitica.models.tasks.TaskType
import com.habitrpg.shared.habitica.serialization.habiticaJson
import com.habitrpg.wearos.habitica.data.repositories.TaskRepository
import com.habitrpg.wearos.habitica.data.repositories.UserRepository
import com.habitrpg.wearos.habitica.managers.AppStateManager
import com.habitrpg.wearos.habitica.models.tasks.Task
import com.habitrpg.wearos.habitica.util.ExceptionHandlerBuilder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskListViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        taskRepository: TaskRepository,
        userRepository: UserRepository,
        val sharedPreferences: SharedPreferences,
        exceptionBuilder: ExceptionHandlerBuilder,
        appStateManager: AppStateManager,
    ) : BaseViewModel(userRepository, taskRepository, exceptionBuilder, appStateManager) {
        val taskType = TaskType.from(savedStateHandle.get<String>("task_type"))
        val taskCount = MutableLiveData(0)
        val completedToDos: MutableList<Task> by lazy {
            val tasksString = sharedPreferences.getString("to_do_tasks", null) ?: return@lazy mutableListOf()
            return@lazy habiticaJson.decodeFromString<MutableList<Task>>(tasksString)
        }
        val tasks =
            taskRepository
                .getTasks(taskType ?: TaskType.HABIT)
                .map {
                    when (taskType) {
                        TaskType.DAILY -> mapDaily(it)
                        TaskType.TODO -> mapTodos(it)
                        else -> map(it)
                    }
                }
        val user =
            userRepository
                .getUser()
                .asLiveData()

        fun scoreTask(
            task: Task,
            direction: TaskDirection,
            onResult: (TaskScoringResult?) -> Unit,
        ) {
            if (taskType == TaskType.TODO) {
                if (direction == TaskDirection.UP && !completedToDos.contains(task)) {
                    completedToDos.add(task)
                } else if (direction == TaskDirection.DOWN) {
                    completedToDos.remove(task)
                }
            }
            viewModelScope.launch(
                exceptionBuilder.userFacing(this) {
                    if (taskType == TaskType.TODO) {
                        if (direction == TaskDirection.UP) {
                            completedToDos.remove(task)
                        } else {
                            completedToDos.add(task)
                        }
                    }
                },
            ) {
                val result =
                    taskRepository.scoreTask(
                        userRepository.getUser().first(),
                        task,
                        direction,
                    )
                if (result?.hasLeveledUp == true) {
                    launch(Dispatchers.Main) {
                        userRepository.retrieveUser()
                    }
                }
                onResult(result)
            }
        }

        private fun map(tasks: List<Task>): List<Task> {
            taskCount.value = tasks.size
            return tasks
        }

        private fun mapDaily(tasks: List<Task>): MutableList<Any> {
            val taskList: MutableList<Any> =
                tasks
                    .filter { it.isDue == true || it.type == TaskType.TODO }
                    .sortedBy { it.completed }
                    .toMutableList()
            val firstCompletedIndex = taskList.indexOfFirst { it is Task && it.completed }
            if (firstCompletedIndex >= 0) {
                // since this is the index of the first completed task, this is also the number of incomplete tasks
                taskCount.value = firstCompletedIndex
                taskList.add(firstCompletedIndex, "Done today")
            } else {
                taskCount.value = taskList.size
            }
            return taskList
        }

        override fun onCleared() {
            saveCurrentToDos()
            super.onCleared()
        }

        private fun mapTodos(tasks: List<Task>): List<Any> {
            val taskList: MutableList<Any> = tasks.filter { !it.completed }.toMutableList()
            taskCount.value = taskList.size
            if (completedToDos.isNotEmpty()) {
                taskList.add("Done today")
                taskList.addAll(completedToDos)
            }

            return taskList
        }

        private fun saveCurrentToDos() {
            sharedPreferences.edit {
                putString("to_do_tasks", habiticaJson.encodeToString<List<Task>>(completedToDos))
            }
        }
    }
