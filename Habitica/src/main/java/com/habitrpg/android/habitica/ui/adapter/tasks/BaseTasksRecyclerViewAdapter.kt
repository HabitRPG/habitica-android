package com.habitrpg.android.habitica.ui.adapter.tasks

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.ui.adapter.BaseRecyclerViewAdapter
import com.habitrpg.android.habitica.ui.viewHolders.BindableViewHolder
import com.habitrpg.android.habitica.ui.viewmodels.TasksViewModel
import com.habitrpg.shared.habitica.models.tasks.TaskType

abstract class BaseTasksRecyclerViewAdapter<VH : BindableViewHolder<Task>>(
    var taskType: TaskType,
    private val viewModel: TasksViewModel,
    private val layoutResource: Int,
    newContext: Context,
    private val userID: String?,
) : BaseRecyclerViewAdapter<Task, VH>() {
    protected var content: MutableList<Task>? = null
    protected var filteredContent: MutableList<Task>? = null
    internal var context: Context = newContext.applicationContext

    init {
        this.filteredContent = ArrayList()
    }

    override fun onBindViewHolder(
        holder: VH,
        position: Int,
    ) {
        val item = filteredContent?.get(position)
        if (item != null) {
            holder.bind(item, position, "normal")
        }
    }

    override fun getItemId(position: Int): Long {
        val task = filteredContent?.get(position)
        return task?.id?.hashCode()?.toLong() ?: 0
    }

    override fun getItemCount(): Int = filteredContent?.size ?: 0

    internal fun getContentView(parent: ViewGroup): View = getContentView(parent, layoutResource)

    protected fun getContentView(
        parent: ViewGroup,
        layoutResource: Int,
    ): View =
        LayoutInflater.from(parent.context).inflate(layoutResource, parent, false)

    private fun updateTask(task: Task) {
        if (taskType != task.type) {
            return
        }
        var i = 0
        while (i < (this.content?.size ?: 0)) {
            if (content?.get(i)?.id == task.id) {
                break
            }
            ++i
        }
        if (i < (content?.size ?: 0)) {
            content?.set(i, task)
        }
        filter()
    }

    fun filter() {
        if (this.viewModel.filterCount(taskType) == 0) {
            filteredContent = content
        } else {
            filteredContent = ArrayList()
            content?.let {
                filteredContent?.addAll(this.viewModel.filter(it))
            }
        }

        this.notifyDataSetChanged()
    }

    fun setTasks(tasks: List<Task>) {
        this.content = ArrayList()
        this.content?.addAll(tasks)
        filter()
    }
}
