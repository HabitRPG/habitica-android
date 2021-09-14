package com.habitrpg.android.habitica.ui.adapter.tasks

import android.view.ViewGroup
import com.habitrpg.android.habitica.helpers.TaskFilterHelper
import com.habitrpg.android.habitica.ui.viewHolders.tasks.HabitViewHolder

class HabitsRecyclerViewAdapter(layoutResource: Int, taskFilterHelper: TaskFilterHelper) : RealmBaseTasksRecyclerViewAdapter<HabitViewHolder>(layoutResource, taskFilterHelper) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder =
        HabitViewHolder(
            getContentView(parent), { task, direction -> taskScoreEventsSubject.onNext(Pair(task, direction)) },
            {
                task ->
                taskOpenEventsSubject.onNext(task)
            }
        ) {
            task ->
            brokenTaskEventsSubject.onNext(task)
        }
}
