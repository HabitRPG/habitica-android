package com.habitrpg.android.habitica.ui.adapter.tasks

import android.view.ViewGroup
import com.habitrpg.android.habitica.helpers.TaskFilterHelper
import com.habitrpg.android.habitica.ui.viewHolders.tasks.TodoViewHolder

class TodosRecyclerViewAdapter(layoutResource: Int, taskFilterHelper: TaskFilterHelper) : RealmBaseTasksRecyclerViewAdapter<TodoViewHolder>(layoutResource, taskFilterHelper) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder =
        TodoViewHolder(
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
