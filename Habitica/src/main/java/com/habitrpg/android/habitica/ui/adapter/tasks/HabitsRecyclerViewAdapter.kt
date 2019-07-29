package com.habitrpg.android.habitica.ui.adapter.tasks

import android.view.ViewGroup
import com.habitrpg.android.habitica.helpers.TaskFilterHelper
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.ui.viewHolders.tasks.HabitViewHolder
import io.realm.OrderedRealmCollection

class HabitsRecyclerViewAdapter(data: OrderedRealmCollection<Task>?, autoUpdate: Boolean, layoutResource: Int, taskFilterHelper: TaskFilterHelper) : RealmBaseTasksRecyclerViewAdapter<HabitViewHolder>(data, autoUpdate, layoutResource, taskFilterHelper) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder =
            HabitViewHolder(getContentView(parent), { task, direction -> taskScoreEventsSubject.onNext(Pair(task, direction)) }) {
                task -> taskOpenEventsSubject.onNext(task)
            }
}