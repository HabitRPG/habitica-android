package com.habitrpg.wearos.habitica.data.repositories

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import com.habitrpg.common.habitica.models.tasks.TaskType
import com.habitrpg.wearos.habitica.models.tasks.Task
import com.habitrpg.wearos.habitica.models.tasks.TaskList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.util.Date
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

    private val taskCountHelperValue = MutableLiveData<Long>()

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
        taskCountHelperValue.value = Date().time
    }

    fun updateTask(task: Task) {
        val oldList = tasks[task.type]?.value?.toMutableList()
        val index = oldList?.indexOfFirst { task.id == it.id }
        if (index != null && index >= 0) {
            oldList[index] = task
        } else {
            oldList?.add(0, task)
        }
        oldList?.let {
            tasks[task.type]?.value = it
        }
        taskCountHelperValue.value = Date().time
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

    fun getTaskCounts() = taskCountHelperValue.asFlow().map {
        mapOf(
            TaskType.HABIT.value to (tasks[TaskType.HABIT]?.value?.size ?: 0),
            TaskType.DAILY.value to (tasks[TaskType.DAILY]?.value?.size ?: 0),
            TaskType.TODO.value to (tasks[TaskType.TODO]?.value?.size ?: 0),
            TaskType.REWARD.value to (tasks[TaskType.REWARD]?.value?.size ?: 0),
        )
    }

    fun getActiveTaskCounts() = taskCountHelperValue.asFlow().map {
        mapOf(
            TaskType.HABIT.value to (tasks[TaskType.HABIT]?.value?.size ?: 0),
            TaskType.DAILY.value to (tasks[TaskType.DAILY]?.value?.filter { it.isDue == true && !it.completed }?.size
                ?: 0),
            TaskType.TODO.value to (tasks[TaskType.TODO]?.value?.filter { !it.completed }?.size
                ?: 0),
            TaskType.REWARD.value to (tasks[TaskType.REWARD]?.value?.size ?: 0),
        )
    }
}