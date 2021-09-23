package com.habitrpg.android.habitica.ui.adapter.tasks

import com.habitrpg.android.habitica.models.responses.TaskDirection
import com.habitrpg.android.habitica.models.tasks.ChecklistItem
import com.habitrpg.android.habitica.models.tasks.Task
import io.reactivex.rxjava3.core.Flowable

interface TaskRecyclerViewAdapter {
    var canScoreTasks: Boolean
    var data: List<Task>

    val errorButtonEvents: Flowable<String>

    var taskDisplayMode: String

    fun filter()

    fun notifyItemMoved(adapterPosition: Int, adapterPosition1: Int)
    fun notifyDataSetChanged()
    fun getItemViewType(position: Int): Int

    fun updateUnfilteredData(data: List<Task>?)

    val taskScoreEvents: Flowable<Pair<Task, TaskDirection>>
    val checklistItemScoreEvents: Flowable<Pair<Task, ChecklistItem>>
    val taskOpenEvents: Flowable<Task>
    val brokenTaskEvents: Flowable<Task>
}
