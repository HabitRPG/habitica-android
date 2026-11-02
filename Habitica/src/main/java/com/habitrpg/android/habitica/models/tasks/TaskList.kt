package com.habitrpg.android.habitica.models.tasks

import com.habitrpg.android.habitica.utils.TaskListSerializer
import kotlinx.serialization.Serializable

@Serializable(with = TaskListSerializer::class)
class TaskList {
    var tasks: MutableMap<String, Task> = mutableMapOf()
}
