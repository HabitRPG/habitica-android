package com.habitrpg.android.habitica.ui.adapter.tasks

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.habitrpg.android.habitica.helpers.TaskFilterHelper
import com.habitrpg.android.habitica.models.responses.TaskDirection
import com.habitrpg.android.habitica.models.tasks.ChecklistItem
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.ui.viewHolders.tasks.BaseTaskViewHolder
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.functions.Action
import io.reactivex.subjects.PublishSubject
import io.realm.OrderedRealmCollection
import io.realm.OrderedRealmCollectionChangeListener
import io.realm.RealmRecyclerViewAdapter

abstract class RealmBaseTasksRecyclerViewAdapter<VH : BaseTaskViewHolder>(private var unfilteredData: OrderedRealmCollection<Task>?, private val hasAutoUpdates: Boolean, private val layoutResource: Int, private val taskFilterHelper: TaskFilterHelper?) : RealmRecyclerViewAdapter<Task, VH>(null, true), TaskRecyclerViewAdapter {
    private var updateOnModification: Boolean = false
    override var ignoreUpdates: Boolean = false
    private val listener: OrderedRealmCollectionChangeListener<OrderedRealmCollection<Task>> by lazy {
        OrderedRealmCollectionChangeListener<OrderedRealmCollection<Task>> { _, changeSet ->
            if (ignoreUpdates) {
                return@OrderedRealmCollectionChangeListener
            }
            // null Changes means the async query returns the first time.
            // For deletions, the adapter has to be notified in reverse order.
            val deletions = changeSet.deletionRanges
            deletions.indices.reversed()
                    .map { deletions[it] }
                    .forEach { notifyItemRangeRemoved(it.startIndex, it.length) }

            val insertions = changeSet.insertionRanges
            for (range in insertions) {
                notifyItemRangeInserted(range.startIndex, range.length)
            }

            if (!updateOnModification) {
                return@OrderedRealmCollectionChangeListener
            }

            val modifications = changeSet.changeRanges
            for (range in modifications) {
                notifyItemRangeChanged(range.startIndex, range.length)
            }
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

    private val isDataValid: Boolean
        get() = data?.isValid ?: false

    init {
        if (unfilteredData != null && unfilteredData?.isManaged == false) {
            throw IllegalStateException("Only use this adapter with managed RealmCollection, " + "for un-managed lists you can just use the BaseRecyclerViewAdapter")
        }
        this.updateOnModification = true
        filter()
    }

    override fun getItemId(index: Int): Long = index.toLong()

    override fun getItemCount(): Int = if (isDataValid) data?.size ?: 0 else 0

    override fun getItem(index: Int): Task? = if (isDataValid) data?.get(index) else null

    override fun updateUnfilteredData(data: OrderedRealmCollection<Task>?) {
        unfilteredData = data
        updateData(data)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        if (item != null) {
            holder.bind(item, position)
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
            updateData(query.sort("position").findAll())
        }
    }

    override fun getTaskIDAt(position: Int): String {
        return data?.get(position)?.id ?: ""
    }
}
