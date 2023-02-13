package com.habitrpg.wearos.habitica.models.tasks

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson

class TaskList(var tasks: MutableMap<String, Task> = mutableMapOf())

class WrappedTasklistAdapter {
    @FromJson
    fun fromJson(json: List<Task>): TaskList {
        val tasks = mutableMapOf<String, Task>()
        json.forEach { tasks[it.id ?: ""] = it }
        return TaskList(tasks)
    }

    @ToJson
    fun toJson(value: TaskList): List<Task> {
        return value.tasks.values.toList()
    }
}
