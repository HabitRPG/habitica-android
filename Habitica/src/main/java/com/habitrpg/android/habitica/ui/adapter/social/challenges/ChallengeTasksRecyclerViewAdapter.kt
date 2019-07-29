package com.habitrpg.android.habitica.ui.adapter.social.challenges

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.helpers.TaskFilterHelper
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.ui.adapter.tasks.SortableTasksRecyclerViewAdapter
import com.habitrpg.android.habitica.ui.viewHolders.BindableViewHolder
import com.habitrpg.android.habitica.ui.viewHolders.tasks.*
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.subjects.PublishSubject

class ChallengeTasksRecyclerViewAdapter(taskFilterHelper: TaskFilterHelper?, layoutResource: Int,
                                        newContext: Context, userID: String, sortCallback: SortTasksCallback?,
                                        private val openTaskDisabled: Boolean, private val taskActionsDisabled: Boolean) : SortableTasksRecyclerViewAdapter<BindableViewHolder<Task>>("", taskFilterHelper, layoutResource, newContext, userID, sortCallback) {

    private val addItemSubject = PublishSubject.create<Task>()

    val taskList: MutableList<Task>
        get() = content?.map { t -> t }?.toMutableList() ?: mutableListOf()

    protected var taskOpenEventsSubject = PublishSubject.create<Task>()
    val taskOpenEvents: Flowable<Task> = taskOpenEventsSubject.toFlowable(BackpressureStrategy.LATEST)

    override fun injectThis(component: UserComponent) {
        component.inject(this)
    }

    override fun loadFromDatabase(): Boolean {
        return false
    }

    override fun getItemViewType(position: Int): Int {
        val task = this.filteredContent?.get(position)

        return when (task?.type) {
            Task.TYPE_HABIT -> TYPE_HABIT
            Task.TYPE_DAILY -> TYPE_DAILY
            Task.TYPE_TODO -> TYPE_TODO
            Task.TYPE_REWARD -> TYPE_REWARD
            else -> if (addItemSubject.hasObservers() && task?.type == TASK_TYPE_ADD_ITEM) TYPE_ADD_ITEM else TYPE_HEADER
        }
    }

    fun addItemObservable(): Flowable<Task> {
        return addItemSubject.toFlowable(BackpressureStrategy.BUFFER)
    }

    fun addTaskUnder(taskToAdd: Task, taskAbove: Task?): Int {
        val position = content?.indexOfFirst { t -> t.id == taskAbove?.id } ?: 0

        content?.add(position + 1, taskToAdd)
        filter()

        return position
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindableViewHolder<Task> {
        val viewHolder: BindableViewHolder<Task> = when (viewType) {
            TYPE_HABIT -> HabitViewHolder(getContentView(parent, R.layout.habit_item_card), { _, _ -> }) { task ->
                taskOpenEventsSubject.onNext(task)
            }
            TYPE_DAILY -> DailyViewHolder(getContentView(parent, R.layout.daily_item_card), { _, _ -> }, { _, _ -> }) { task ->
                taskOpenEventsSubject.onNext(task)
            }
            TYPE_TODO -> TodoViewHolder(getContentView(parent, R.layout.todo_item_card), { _, _ -> }, { _, _ -> }) { task ->
                taskOpenEventsSubject.onNext(task)
            }
            TYPE_REWARD -> RewardViewHolder(getContentView(parent, R.layout.reward_item_card), { _, _ -> }) { task ->
                taskOpenEventsSubject.onNext(task)
            }
            TYPE_ADD_ITEM -> AddItemViewHolder(getContentView(parent, R.layout.challenge_add_task_item), addItemSubject)
            else -> DividerViewHolder(getContentView(parent, R.layout.challenge_task_divider))
        }

        (viewHolder as? BaseTaskViewHolder)?.setDisabled(openTaskDisabled, taskActionsDisabled)
        return viewHolder
    }


    /**
     * @param task
     * @return true if task found&updated
     */
    fun replaceTask(task: Task): Boolean {
        var i = 0
        while (i < this.content?.size ?: 0) {
            if (content?.get(i)?.id == task.id) {
                break
            }
            ++i
        }
        if (i < content?.size ?: 0) {
            content?.set(i, task)

            filter()
            return true
        }

        return false
    }

    inner class AddItemViewHolder internal constructor(itemView: View, private val callback: PublishSubject<Task>) : BindableViewHolder<Task>(itemView) {

        private val addBtn: Button = itemView.findViewById(R.id.btn_add_task)
        private var newTask: Task? = null

        init {
            addBtn.isClickable = true
            addBtn.setOnClickListener { newTask?.let { callback.onNext(it) } }
        }

        override fun bind(newTask: Task, position: Int) {
            this.newTask = newTask
            addBtn.text = newTask.text
        }
    }

    private inner class DividerViewHolder internal constructor(itemView: View) : BindableViewHolder<Task>(itemView) {

        private val dividerName: TextView = itemView.findViewById(R.id.divider_name)

        override fun bind(newTask: Task, position: Int) {
            dividerName.text = newTask.text
        }
    }

    companion object {
        const val TASK_TYPE_ADD_ITEM = "ADD_ITEM"

        private const val TYPE_HEADER = 0
        private const val TYPE_HABIT = 1
        private const val TYPE_DAILY = 2
        private const val TYPE_TODO = 3
        private const val TYPE_REWARD = 4
        private const val TYPE_ADD_ITEM = 5
    }
}
