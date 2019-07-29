package com.habitrpg.android.habitica.ui.adapter.tasks

import com.habitrpg.android.habitica.models.responses.TaskDirection
import com.habitrpg.android.habitica.models.tasks.ChecklistItem
import com.habitrpg.android.habitica.models.tasks.Task
import io.reactivex.Flowable
import io.realm.OrderedRealmCollection

interface TaskRecyclerViewAdapter {
    var ignoreUpdates: Boolean

    val errorButtonEvents: Flowable<String>

    fun updateData(tasks: OrderedRealmCollection<Task>?)

    fun filter()

    fun notifyItemMoved(adapterPosition: Int, adapterPosition1: Int)
    fun notifyDataSetChanged()
    fun getItemViewType(position: Int): Int
    fun getTaskIDAt(position: Int): String?

    fun updateUnfilteredData(data: OrderedRealmCollection<Task>?)

    val taskScoreEvents: Flowable<Pair<Task, TaskDirection>>
    val checklistItemScoreEvents: Flowable<Pair<Task, ChecklistItem>>
    val taskOpenEvents: Flowable<Task>
}
