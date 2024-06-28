package com.habitrpg.shared.habitica.models.tasks

class TasksOrder {
    fun positionOf(
        key: String,
        type: TaskType
    ): Int {
        return when (type) {
            TaskType.HABIT -> habits.indexOf(key)
            TaskType.DAILY -> dailys.indexOf(key)
            TaskType.TODO -> todos.indexOf(key)
            TaskType.REWARD -> rewards.indexOf(key)
        }
    }

    var habits: List<String> = listOf()
    var dailys: List<String> = listOf()
    var todos: List<String> = listOf()
    var rewards: List<String> = listOf()
}
