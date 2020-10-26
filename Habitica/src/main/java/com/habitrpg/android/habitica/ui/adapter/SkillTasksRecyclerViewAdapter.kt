package com.habitrpg.android.habitica.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.SkillTaskItemCardBinding
import com.habitrpg.shared.habitica.models.tasks.Task
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.subjects.PublishSubject
import io.realm.OrderedRealmCollection
import io.realm.RealmRecyclerViewAdapter
import java.util.*


class SkillTasksRecyclerViewAdapter(data: OrderedRealmCollection<Task>?, autoUpdate: Boolean) : RealmRecyclerViewAdapter<Task, SkillTasksRecyclerViewAdapter.TaskViewHolder>(data, autoUpdate) {

    private val taskSelectionEvents = PublishSubject.create<Task>()

    override fun getItemId(position: Int): Long {
        if (data != null) {
            val task = data?.get(position)
            if (task?.id?.length == 36) {
                return UUID.fromString(task.id).mostSignificantBits
            }
        }
        return UUID.randomUUID().mostSignificantBits
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.skill_task_item_card, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        data?.let {
            holder.bindHolder(it[position])
        }
    }

    fun getTaskSelectionEvents(): Flowable<Task> {
        return taskSelectionEvents.toFlowable(BackpressureStrategy.DROP)
    }

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        private val binding = SkillTaskItemCardBinding.bind(itemView)
        var task: Task? = null

        init {
            itemView.setOnClickListener(this)
            itemView.isClickable = true
        }

        internal fun bindHolder(task: Task) {
            this.task = task
            binding.titleTextView.text = task.markdownText { binding.titleTextView.text = it }
            if (task.notes?.isEmpty() == true) {
                binding.notesTextView.visibility = View.GONE
            } else {
                binding.notesTextView.visibility = View.VISIBLE
                binding.notesTextView.text = task.markdownNotes { binding.notesTextView.text = it }
            }
            binding.rightBorderView.setBackgroundResource(task.lightTaskColor)
        }

        override fun onClick(v: View) {
            if (v == itemView) {
                task?.let {
                    taskSelectionEvents.onNext(it)
                }
            }
        }
    }
}
