package com.habitrpg.wearos.habitica.ui.viewmodels

import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.habitrpg.common.habitica.models.responses.TaskDirection
import com.habitrpg.common.habitica.models.responses.TaskScoringResult
import com.habitrpg.common.habitica.models.tasks.TaskType
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
import java.lang.reflect.Type
import java.util.ArrayList
import javax.inject.Inject

@HiltViewModel
class TaskListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    taskRepository: TaskRepository,
    userRepository: UserRepository,
    val sharedPreferences: SharedPreferences,
    exceptionBuilder: ExceptionHandlerBuilder, appStateManager: AppStateManager
) : BaseViewModel(userRepository, taskRepository, exceptionBuilder, appStateManager) {
    private val gson = Gson()
    private val tasksString = sharedPreferences.getString("to_do_tasks", null)
    val taskType = TaskType.from(savedStateHandle.get<String>("task_type"))
    val taskCount = MutableLiveData(0)
    val tasks = taskRepository.getTasks(taskType ?: TaskType.HABIT)
        .map {
            when(taskType) {
                TaskType.DAILY -> mapDaily(it)
                TaskType.TODO -> mapTodos(it)
                else -> map(it)
            }
        }
        .asLiveData()
    val user = userRepository.getUser()
        .asLiveData()

    fun scoreTask(task: Task, direction: TaskDirection, onResult: (TaskScoringResult?) -> Unit) {
        viewModelScope.launch(exceptionBuilder.userFacing(this)) {
            val result = taskRepository.scoreTask(
                userRepository.getUser().first(),
                task,
                direction
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
        val taskList: MutableList<Any> = tasks.filter { it.isDue == true || it.type == TaskType.TODO }.sortedBy { it.completed }.toMutableList()
        val firstCompletedIndex = taskList.indexOfFirst { it is Task &&  it.completed }
        if (firstCompletedIndex >= 0) {
            // since this is the index of the first completed task, this is also the number of incomplete tasks
            taskCount.value = firstCompletedIndex
            taskList.add(firstCompletedIndex, "Done today")
        } else {
            taskCount.value = taskList.size
        }
        return taskList
    }

    private fun getCurrentToDos(): List<Any>? {
        val gson = Gson()
        val data = mutableListOf<Any>()
        val tasksString = sharedPreferences.getString("to_do_tasks", null)
        if (tasksString != null) {
            val type: Type = object : TypeToken<ArrayList<Task?>?>() {}.type
            val savedCurrentTasks = gson.fromJson(tasksString, type) as MutableList<Task>
            val list = savedCurrentTasks.sortedBy { it.completed }
            val firstCompletedIndex = list.indexOfFirst { it.completed }
            return if (firstCompletedIndex >= 0) {
                // since this is the index of the first completed task, this is also the number of incomplete tasks
                taskCount.value = firstCompletedIndex
                data.addAll(list)
                data.add(firstCompletedIndex, "Done today")
                data
            } else {
                savedCurrentTasks
            }
        }
        return null
    }

    private fun mapTodos(tasks: List<Task>): List<Any>? {
        saveCurrentToDos(tasks)
        return getCurrentToDos()
    }

    private fun saveCurrentToDos(tasks: List<Task>) {
        val taskList = mutableListOf<Task>()
        val type: Type = object : TypeToken<ArrayList<Task?>?>() {}.type
        if (tasksString != null) {
            val savedCurrentTasks = gson.fromJson(tasksString, type) as MutableList<Task>
            if (!savedCurrentTasks.isNullOrEmpty()) {
                for (task in tasks) {
                    if (!savedCurrentTasks.contains(task)) {
                        taskList.add(task)
                    }
                }
            }
        } else {
            taskList.addAll(tasks)
        }
        if (!taskList.isNullOrEmpty()) {
            sharedPreferences.edit {
                putString("to_do_tasks", gson.toJson(taskList))
            }
        }
    }

    fun setCurrentToDoAsComplete(currentTask: Task) {
        val gson = Gson()
        val type: Type = object : TypeToken<ArrayList<Task?>?>() {}.type
        if (tasksString != null) {
            val savedCurrentTasks = gson.fromJson(tasksString, type) as MutableList<Task>
            if (!savedCurrentTasks.isNullOrEmpty()) {
                savedCurrentTasks.let { tasks ->
                    val task = tasks[tasks.indexOf(currentTask)]
                    task.completed = !task.completed
                    tasks[tasks.indexOf(currentTask)] = task
                    sharedPreferences.edit {
                        putString("to_do_tasks", gson.toJson(tasks))
                    }
                }
            }
        }
    }

}