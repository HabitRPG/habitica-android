package com.habitrpg.shared.habitica.models.tasks

expect class TasksOrder {
    internal var habits: List<String>
    internal var dailys: List<String>
    internal var todos: List<String>
    internal var rewards: List<String>

    fun getHabits(): List<String>

    fun setHabits(habits: List<String>)

    fun getDailys(): List<String>

    fun setDailys(dailys: List<String>)

    fun getTodos(): List<String>

    fun setTodos(todos: List<String>)

    fun getRewards(): List<String>

    fun setRewards(rewards: List<String>)
}
