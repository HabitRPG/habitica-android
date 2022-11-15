package com.habitrpg.android.habitica.ui.adapter.tasks

import android.view.View
import com.habitrpg.android.habitica.models.tasks.ChecklistItem
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.shared.habitica.models.responses.TaskDirection
import io.reactivex.rxjava3.core.Flowable

interface TaskRecyclerViewAdapter {
    var user: User?
    var showAdventureGuide: Boolean
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
    val taskOpenEvents: Flowable<Pair<Task, View>>
    val brokenTaskEvents: Flowable<Task>
    val adventureGuideOpenEvents: Flowable<Boolean>?
}
