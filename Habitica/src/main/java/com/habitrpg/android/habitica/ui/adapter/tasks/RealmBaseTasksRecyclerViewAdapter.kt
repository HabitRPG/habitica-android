package com.habitrpg.android.habitica.ui.adapter.tasks

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.habitrpg.android.habitica.helpers.TaskFilterHelper
import com.habitrpg.android.habitica.models.responses.TaskDirection
import com.habitrpg.android.habitica.models.tasks.ChecklistItem
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.ui.adapter.BaseRecyclerViewAdapter
import com.habitrpg.android.habitica.ui.viewHolders.tasks.BaseTaskViewHolder
import com.habitrpg.android.habitica.ui.viewHolders.tasks.RewardViewHolder
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.functions.Action
import io.reactivex.rxjava3.subjects.PublishSubject
import io.realm.OrderedRealmCollection

abstract class RealmBaseTasksRecyclerViewAdapter<VH : BaseTaskViewHolder>(
        private var unfilteredData: OrderedRealmCollection<Task>?,
        private val hasAutoUpdates: Boolean,
        private val layoutResource: Int,
        private val taskFilterHelper: TaskFilterHelper?
) : BaseRecyclerViewAdapter<Task, VH>(), TaskRecyclerViewAdapter {
    override var canScoreTasks = true

    private var updateOnModification: Boolean = false
    override var ignoreUpdates: Boolean = false

    override var taskDisplayMode: String = "standard"
    set(value) {
        if (field != value) {
            field = value
            notifyDataSetChanged()
        }
    }

    private var errorButtonEventsSubject = PublishSubject.create<String>()
    override val errorButtonEvents: Flowable<String> = errorButtonEventsSubject.toFlowable(BackpressureStrategy.DROP)
    protected var taskScoreEventsSubject = PublishSubject.create<Pair<Task, TaskDirection>>()
    override val taskScoreEvents: Flowable<Pair<Task, TaskDirection>> = taskScoreEventsSubject.toFlowable(BackpressureStrategy.DROP)
    protected var checklistItemScoreSubject = PublishSubject.create<Pair<Task, ChecklistItem>>()
    override val checklistItemScoreEvents: Flowable<Pair<Task, ChecklistItem>> = checklistItemScoreSubject.toFlowable(BackpressureStrategy.DROP)
    protected var taskOpenEventsSubject = PublishSubject.create<Task>()
    override val taskOpenEvents: Flowable<Task> = taskOpenEventsSubject.toFlowable(BackpressureStrategy.DROP)
    protected var brokenTaskEventsSubject = PublishSubject.create<Task>()
    override val brokenTaskEvents: Flowable<Task> = brokenTaskEventsSubject.toFlowable(BackpressureStrategy.DROP)

    init {
        check(!(unfilteredData != null && unfilteredData?.isManaged == false)) { "Only use this adapter with managed RealmCollection, " + "for un-managed lists you can just use the BaseRecyclerViewAdapter" }
        this.updateOnModification = true
        filter()
    }

    override fun getItemId(index: Int): Long = index.toLong()

    override fun updateUnfilteredData(data: OrderedRealmCollection<Task>?) {
        unfilteredData = data
        this.data = data ?: emptyList()
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        if (item != null) {
            holder.isLocked = !canScoreTasks
            holder.bind(item, position, taskDisplayMode)
            holder.errorButtonClicked = Action {
                errorButtonEventsSubject.onNext("")
            }
        }
    }

    internal fun getContentView(parent: ViewGroup): View = getContentView(parent, layoutResource)

    private fun getContentView(parent: ViewGroup, layoutResource: Int): View =
            LayoutInflater.from(parent.context).inflate(layoutResource, parent, false)

    final override fun filter() {
        val unfilteredData = this.unfilteredData ?: return

        if (taskFilterHelper != null) {
            val query = taskFilterHelper.createQuery(unfilteredData)
            if (query != null) {
                data = query.findAll()
            }
        }
    }

    override fun getTaskIDAt(position: Int): String {
        return data[position].id ?: ""
    }
}
