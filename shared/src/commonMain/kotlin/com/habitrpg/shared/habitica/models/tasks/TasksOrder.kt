package com.habitrpg.shared.habitica.models.tasks

expect class TasksOrder {
    internal var habits: List<String>?
    internal var dailys: List<String>?
    internal var todos: List<String>?
    internal var rewards: List<String>?
}
