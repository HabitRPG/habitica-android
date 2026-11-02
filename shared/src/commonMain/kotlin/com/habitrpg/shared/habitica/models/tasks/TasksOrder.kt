package com.habitrpg.shared.habitica.models.tasks

import kotlinx.serialization.Serializable

@Serializable
class TasksOrder {
    var habits: List<String> = listOf()
    var dailys: List<String> = listOf()
    var todos: List<String> = listOf()
    var rewards: List<String> = listOf()
}
