package com.habitrpg.android.habitica.ui.adapter.tasks

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.habitrpg.android.habitica.helpers.TaskFilterHelper
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.ui.viewHolders.tasks.BaseTaskViewHolder
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.functions.Action
import io.reactivex.subjects.PublishSubject
import io.realm.OrderedRealmCollection
import io.realm.OrderedRealmCollectionChangeListener
import io.realm.RealmList
import io.realm.RealmResults

abstract class RealmBaseTasksRecyclerViewAdapter<VH : BaseTaskViewHolder>(private var unfilteredData: OrderedRealmCollection<Task>?, private val hasAutoUpdates: Boolean, private val layoutResource: Int, private val taskFilterHelper: TaskFilterHelper?) : androidx.recyclerview.widget.RecyclerView.Adapter<VH>(), TaskRecyclerViewAdapter {
    private var updateOnModification: Boolean = false
    private var ignoreUpdates: Boolean = false
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
    var data: OrderedRealmCollection<Task>? = null
        private set

    private var errorButtonEventsSubject = PublishSubject.create<String>()

    private val isDataValid: Boolean
        get() = data?.isValid ?: false

    init {
        if (unfilteredData != null && unfilteredData?.isManaged == false) {
            throw IllegalStateException("Only use this adapter with managed RealmCollection, " + "for un-managed lists you can just use the BaseRecyclerViewAdapter")
        }
        this.data = unfilteredData
        this.updateOnModification = true
        filter()
    }

    override fun onAttachedToRecyclerView(recyclerView: androidx.recyclerview.widget.RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        if (hasAutoUpdates && isDataValid) {
            addListener(data)
        }
    }

    override fun onDetachedFromRecyclerView(recyclerView: androidx.recyclerview.widget.RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        if (hasAutoUpdates && isDataValid) {

            removeListener(data)
        }
    }

    override fun getItemId(index: Int): Long = index.toLong()

    override fun getItemCount(): Int = if (isDataValid) data?.size ?: 0 else 0

    fun getItem(index: Int): Task? = if (isDataValid) data?.get(index) else null

    override fun updateData(data: OrderedRealmCollection<Task>?) {
        if (hasAutoUpdates) {
            if (isDataValid) {

                removeListener(this.data)
            }
            if (data != null) {
                addListener(data)
            }
        }

        this.data = data
        notifyDataSetChanged()
    }

    override fun updateUnfilteredData(data: OrderedRealmCollection<Task>?) {
        unfilteredData = data
        updateData(data)
    }

    @Suppress("UNCHECKED_CAST")
    private fun addListener(data: OrderedRealmCollection<Task>?) = when (data) {
        is RealmResults<*> -> {
            val results = data as RealmResults<Task>
            results.addChangeListener(listener as OrderedRealmCollectionChangeListener<RealmResults<Task>>)
        }
        is RealmList<*> -> {
            val list = data as RealmList<Task>
            list.addChangeListener(listener as OrderedRealmCollectionChangeListener<RealmList<Task>>)
        }
        else -> throw IllegalArgumentException("RealmCollection not supported: " + data?.javaClass)
    }

    @Suppress("UNCHECKED_CAST")
    private fun removeListener(data: OrderedRealmCollection<Task>?) {
        when (data) {
            is RealmResults<*> -> {
                val results = data as RealmResults<Task>
                results.removeChangeListener(listener as OrderedRealmCollectionChangeListener<RealmResults<Task>>)
            }
            is RealmList<*> -> {
                val list = data as RealmList<Task>
                list.removeChangeListener(listener as OrderedRealmCollectionChangeListener<RealmList<Task>>)
            }
            else -> throw IllegalArgumentException("RealmCollection not supported: " + data?.javaClass)
        }
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        if (item != null) {
            holder.bindHolder(item, position)
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

    override fun getIgnoreUpdates(): Boolean = ignoreUpdates

    override fun setIgnoreUpdates(ignoreUpdates: Boolean) {
        this.ignoreUpdates = ignoreUpdates
    }

    override fun getTaskIDAt(position: Int): String {
        return data?.get(position)?.id ?: ""
    }

    override fun getErrorButtonEvents(): Flowable<String> {
        return errorButtonEventsSubject.toFlowable(BackpressureStrategy.DROP)
    }
}
