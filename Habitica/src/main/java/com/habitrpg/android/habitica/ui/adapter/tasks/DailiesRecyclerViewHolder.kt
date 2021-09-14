package com.habitrpg.android.habitica.ui.adapter.tasks

import android.view.ViewGroup
import com.habitrpg.android.habitica.helpers.TaskFilterHelper
import com.habitrpg.android.habitica.ui.viewHolders.tasks.DailyViewHolder

class DailiesRecyclerViewHolder(layoutResource: Int, taskFilterHelper: TaskFilterHelper) : RealmBaseTasksRecyclerViewAdapter<DailyViewHolder>(layoutResource, taskFilterHelper) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DailyViewHolder =
        DailyViewHolder(
            getContentView(parent), { task, direction -> taskScoreEventsSubject.onNext(Pair(task, direction)) },
            { task, item -> checklistItemScoreSubject.onNext(Pair(task, item)) },
            {
                task ->
                taskOpenEventsSubject.onNext(task)
            }
        ) {
            task ->
            brokenTaskEventsSubject.onNext(task)
        }
}
