package com.habitrpg.android.habitica.ui.adapter.tasks

import android.view.View
import com.habitrpg.android.habitica.models.tasks.ChecklistItem
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.shared.habitica.models.responses.TaskDirection

interface TaskRecyclerViewAdapter {
    var user: User?
    var showAdventureGuide: Boolean
    var data: List<Task>

    var errorButtonEvents: ((String) -> Unit)?

    var taskDisplayMode: String

    fun filter()

    fun notifyItemMoved(adapterPosition: Int, adapterPosition1: Int)
    fun notifyDataSetChanged()
    fun getItemViewType(position: Int): Int

    fun updateUnfilteredData(data: List<Task>?)

    var taskScoreEvents: ((Task, TaskDirection) -> Unit)?
    var checklistItemScoreEvents: ((Task, ChecklistItem) -> Unit)?
    var taskOpenEvents: ((Task, View) -> Unit)?
    var brokenTaskEvents: ((Task) -> Unit)?
    var adventureGuideOpenEvents: ((Boolean) -> Unit)?
}
