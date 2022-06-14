package com.habitrpg.wearos.habitica.data.repositories

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import com.habitrpg.common.habitica.models.tasks.TaskType
import com.habitrpg.wearos.habitica.models.tasks.Task
import com.habitrpg.wearos.habitica.models.tasks.TaskList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskLocalRepository @Inject constructor() {
    private val tasks = mapOf(
        TaskType.HABIT to MutableLiveData<List<Task>>(),
        TaskType.DAILY to MutableLiveData<List<Task>>(),
        TaskType.TODO to MutableLiveData<List<Task>>(),
        TaskType.REWARD to MutableLiveData<List<Task>>()
    )
    fun getTasks(type: TaskType): Flow<List<Task>> {
        return tasks[type]?.asFlow() ?: emptyFlow()
    }

    fun saveTasks(tasks: TaskList) {
        val taskMap = mutableMapOf(
            TaskType.HABIT to mutableListOf<Task>(),
            TaskType.DAILY to mutableListOf<Task>(),
            TaskType.TODO to mutableListOf<Task>(),
            TaskType.REWARD to mutableListOf<Task>()
        )
        for (task in tasks.tasks) {
            if (task.value.type != null) {
                taskMap[task.value.type]?.add(task.value)
            }
        }
        for (type in taskMap) {
            this.tasks[type.key]?.value = type.value
        }
    }

    fun updateTask(task: Task) {
        val oldList = tasks[task.type]?.value?.toMutableList()
        oldList?.indexOfFirst { task.id == it.id }?.let { index ->
            oldList.set(index, task)
        }
        tasks[task.type]?.value = oldList
    }

    fun getTask(taskID: String): Flow<Task?> {
        for (type in tasks.values) {
            val task = type.value?.firstOrNull { it.id == taskID }
            if (task != null) {
                return flowOf(task)
            }
        }
        return emptyFlow()
    }
}