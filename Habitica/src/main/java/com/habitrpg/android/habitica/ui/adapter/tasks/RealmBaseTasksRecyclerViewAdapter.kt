package com.habitrpg.android.habitica.ui.adapter.tasks

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.habitrpg.android.habitica.helpers.TaskFilterHelper
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.ui.viewHolders.tasks.BaseTaskViewHolder
import io.realm.OrderedRealmCollection
import io.realm.OrderedRealmCollectionChangeListener
import io.realm.RealmList
import io.realm.RealmResults

abstract class RealmBaseTasksRecyclerViewAdapter<VH : BaseTaskViewHolder>(private val unfilteredData: OrderedRealmCollection<Task>?, private val hasAutoUpdates: Boolean, private val layoutResource: Int, private val taskFilterHelper: TaskFilterHelper?) : RecyclerView.Adapter<VH>(), TaskRecyclerViewAdapter {
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

    private val isDataValid: Boolean
        get() = data?.isValid ?: false

    init {
        if (unfilteredData != null && !unfilteredData.isManaged)
            throw IllegalStateException("Only use this adapter with managed RealmCollection, " + "for un-managed lists you can just use the BaseRecyclerViewAdapter")
        this.data = unfilteredData
        this.updateOnModification = true
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView?) {
        super.onAttachedToRecyclerView(recyclerView)
        if (hasAutoUpdates && isDataValid) {
            addListener(data)
        }
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView?) {
        super.onDetachedFromRecyclerView(recyclerView)
        if (hasAutoUpdates && isDataValid) {

            removeListener(data)
        }
    }

    override fun getItemId(index: Int): Long = index.toLong()

    override fun getItemCount(): Int = if (isDataValid) data!!.size else 0

    fun getItem(index: Int): Task? = if (isDataValid) data!![index] else null

    override fun updateData(data: OrderedRealmCollection<Task>?) {
        if (hasAutoUpdates) {
            if (isDataValid) {

                removeListener(this.data!!)
            }
            if (data != null) {
                addListener(data)
            }
        }

        this.data = data
        notifyDataSetChanged()
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
        }
    }


    internal fun getContentView(parent: ViewGroup): View = getContentView(parent, layoutResource)

    private fun getContentView(parent: ViewGroup, layoutResource: Int): View =
            LayoutInflater.from(parent.context).inflate(layoutResource, parent, false)

    override fun filter() {
        if (unfilteredData == null) {
            return
        }

        if (taskFilterHelper != null) {
            val query = taskFilterHelper.createQuery(unfilteredData)
            updateData(query.findAllSorted("position"))

        }
    }

    override fun getIgnoreUpdates(): Boolean = ignoreUpdates

    override fun setIgnoreUpdates(ignoreUpdates: Boolean) {
        this.ignoreUpdates = ignoreUpdates
    }
}
