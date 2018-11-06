package com.habitrpg.android.habitica.ui.adapter

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.extensions.notNull
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.ui.helpers.bindView
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.subjects.PublishSubject
import io.realm.OrderedRealmCollection
import io.realm.RealmRecyclerViewAdapter
import net.pherth.android.emoji_library.EmojiTextView
import java.util.*


class SkillTasksRecyclerViewAdapter(data: OrderedRealmCollection<Task>?, autoUpdate: Boolean) : RealmRecyclerViewAdapter<Task, SkillTasksRecyclerViewAdapter.TaskViewHolder>(data, autoUpdate) {

    private val taskSelectionEvents = PublishSubject.create<Task>()

    override fun getItemId(position: Int): Long {
        if (data != null) {
            val task = data!![position]
            if (task.id != null && task.id!!.length == 36) {
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
        data.notNull {
            holder.bindHolder(it[position])
        }
    }

    fun getTaskSelectionEvents(): Flowable<Task> {
        return taskSelectionEvents.toFlowable(BackpressureStrategy.DROP)
    }

    inner class TaskViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView), View.OnClickListener {

        var task: Task? = null

        private val titleTextView: EmojiTextView by bindView(R.id.titleTextView)
        private val notesTextView: EmojiTextView by bindView(R.id.notesTextView)
        private val rightBorderView: View by bindView(R.id.rightBorderView)

        init {
            itemView.setOnClickListener(this)
            itemView.isClickable = true
        }

        internal fun bindHolder(task: Task) {
            this.task = task
            titleTextView.text = task.markdownText { titleTextView.text = it }
            if (task.notes?.isEmpty() == true) {
                notesTextView.visibility = View.GONE
            } else {
                notesTextView.visibility = View.VISIBLE
                notesTextView.text = task.markdownNotes { notesTextView.text = it }
            }
            rightBorderView.setBackgroundResource(task.lightTaskColor)
        }

        override fun onClick(v: View) {
            if (v == itemView) {
                task.notNull {
                    taskSelectionEvents.onNext(it)
                }
            }
        }
    }
}
