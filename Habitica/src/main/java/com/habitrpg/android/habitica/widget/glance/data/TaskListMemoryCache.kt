package com.habitrpg.android.habitica.widget.glance.data

import com.habitrpg.shared.habitica.models.tasks.TaskType
import java.util.concurrent.ConcurrentHashMap

object TaskListMemoryCache {
    private val cache = ConcurrentHashMap<TaskType, TaskListWidgetState>()

    fun get(taskType: TaskType): TaskListWidgetState? = cache[taskType]

    fun put(taskType: TaskType, state: TaskListWidgetState) {
        cache[taskType] = state
    }

    fun clear() {
        cache.clear()
    }
}
