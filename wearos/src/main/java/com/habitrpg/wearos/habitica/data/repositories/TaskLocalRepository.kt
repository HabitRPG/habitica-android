package com.habitrpg.wearos.habitica.data.repositories

import com.habitrpg.shared.habitica.models.tasks.TaskType
import com.habitrpg.shared.habitica.models.tasks.TasksOrder
import com.habitrpg.wearos.habitica.models.tasks.Task
import com.habitrpg.wearos.habitica.models.tasks.TaskList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskLocalRepository @Inject constructor() {
    private val tasks = mapOf(
        TaskType.HABIT to MutableStateFlow<List<Task>?>(null),
        TaskType.DAILY to MutableStateFlow<List<Task>?>(null),
        TaskType.TODO to MutableStateFlow<List<Task>?>(null),
        TaskType.REWARD to MutableStateFlow<List<Task>?>(null)
    )

    private val taskCountHelperValue = MutableStateFlow<Long>(0)

    fun getTasks(type: TaskType): Flow<List<Task>> {
        return tasks[type]!!.filterNotNull()
    }

    fun saveTasks(tasks: TaskList, order: TasksOrder?) {
        val taskMap = mapOf(
            TaskType.HABIT to sortTasks(tasks.tasks, order?.habits ?: emptyList(), TaskType.HABIT),
            TaskType.DAILY to sortTasks(tasks.tasks, order?.dailys ?: emptyList(), TaskType.DAILY),
            TaskType.TODO to sortTasks(tasks.tasks, order?.todos ?: emptyList(), TaskType.TODO),
            TaskType.REWARD to sortTasks(tasks.tasks, order?.rewards ?: emptyList(), TaskType.REWARD)
        )
        for (type in taskMap) {
            this.tasks[type.key]?.value = null
            this.tasks[type.key]?.value = type.value
        }
        taskCountHelperValue.value = Date().time
    }

    private fun sortTasks(taskMap: MutableMap<String, Task>, taskOrder: List<String>, type: TaskType): List<Task> {
        val taskList = ArrayList<Task>()
        var position = 0
        for (taskId in taskOrder) {
            val task = taskMap[taskId]
            if (task != null) {
                task.position = position
                taskList.add(task)
                position++
                taskMap.remove(taskId)
            }
        }
        for (task in taskMap.values) {
            if (task.type != type) continue
            task.position = position
            taskList.add(task)
            position++
        }
        return taskList
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
            tasks[task.type]?.value = null
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

    fun getTaskCounts() = taskCountHelperValue.map {
        mapOf(
            TaskType.HABIT.value to (tasks[TaskType.HABIT]?.value?.size ?: 0),
            TaskType.DAILY.value to (tasks[TaskType.DAILY]?.value?.size ?: 0),
            TaskType.TODO.value to (tasks[TaskType.TODO]?.value?.size ?: 0),
            TaskType.REWARD.value to (tasks[TaskType.REWARD]?.value?.size ?: 0),
        )
    }

    fun getActiveTaskCounts() = taskCountHelperValue.map {
        mapOf(
            TaskType.HABIT.value to (tasks[TaskType.HABIT]?.value?.size ?: 0),
            TaskType.DAILY.value to (
                tasks[TaskType.DAILY]?.value?.filter { it.isDue == true && !it.completed }?.size
                    ?: 0
                ),
            TaskType.TODO.value to (
                tasks[TaskType.TODO]?.value?.filter { !it.completed }?.size
                    ?: 0
                ),
            TaskType.REWARD.value to (tasks[TaskType.REWARD]?.value?.size ?: 0),
        )
    }

    fun clearData() {
        tasks.values.forEach {
            it.value = emptyList()
        }
        taskCountHelperValue.value = 0
    }
}
