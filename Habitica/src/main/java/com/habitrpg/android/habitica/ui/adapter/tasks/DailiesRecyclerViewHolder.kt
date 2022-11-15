package com.habitrpg.android.habitica.ui.adapter.tasks

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.habitrpg.android.habitica.ui.viewHolders.tasks.DailyViewHolder
import com.habitrpg.android.habitica.ui.viewmodels.TasksViewModel

class DailiesRecyclerViewHolder(layoutResource: Int, viewModel: TasksViewModel) : RealmBaseTasksRecyclerViewAdapter(layoutResource, viewModel) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == 0) {
            DailyViewHolder(
                getContentView(parent), { task, direction -> taskScoreEventsSubject.onNext(Pair(task, direction)) },
                { task, item -> checklistItemScoreSubject.onNext(Pair(task, item)) },
                {
                        task ->
                    taskOpenEventsSubject.onNext(task)
                }, {
                    task ->
                brokenTaskEventsSubject.onNext(task)
            }, viewModel)
        } else {
            super.onCreateViewHolder(parent, viewType)
        }
    }
}
