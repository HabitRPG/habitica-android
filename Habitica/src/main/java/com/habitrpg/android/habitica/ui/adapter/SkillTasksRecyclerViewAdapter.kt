package com.habitrpg.android.habitica.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.SkillTaskItemCardBinding
import com.habitrpg.android.habitica.models.tasks.Task
import java.util.UUID

class SkillTasksRecyclerViewAdapter :
    BaseRecyclerViewAdapter<Task, SkillTasksRecyclerViewAdapter.TaskViewHolder>() {
    var onTaskSelection: ((Task) -> Unit)? = null

    override fun getItemId(position: Int): Long {
        val task = getItem(position)
        if (task?.id?.length == 36) {
            return UUID.fromString(task.id).mostSignificantBits
        }
        return UUID.randomUUID().mostSignificantBits
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): TaskViewHolder {
        val view =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.skill_task_item_card, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: TaskViewHolder,
        position: Int,
    ) {
        holder.bindHolder(data[position])
    }

    inner class TaskViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
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
            binding.rightBorderView.setBackgroundColor(
                ContextCompat.getColor(
                    itemView.context,
                    task.lightTaskColor,
                ),
            )
        }

        override fun onClick(v: View) {
            if (v == itemView) {
                task?.let {
                    onTaskSelection?.invoke(it)
                }
            }
        }
    }
}
