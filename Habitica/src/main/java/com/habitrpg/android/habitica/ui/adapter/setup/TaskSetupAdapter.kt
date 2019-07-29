package com.habitrpg.android.habitica.ui.adapter.setup

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.extensions.inflate
import com.habitrpg.android.habitica.ui.helpers.bindView
import java.util.*

class TaskSetupAdapter : RecyclerView.Adapter<TaskSetupAdapter.TaskViewHolder>() {

    var checkedList: MutableList<Boolean> = mutableListOf()
    private var taskList: List<List<String>> = emptyList()

    fun setTaskList(taskList: List<List<String>>) {
        this.taskList = taskList
        this.checkedList = ArrayList()
        for (ignored in this.taskList) {
            this.checkedList.add(false)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        return TaskViewHolder(parent.inflate(R.layout.task_setup_item))
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(this.taskList[position], this.checkedList[position])
    }

    override fun getItemCount(): Int {
        return this.taskList.size
    }

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        private val icon: Drawable?
        private val textView: TextView by bindView(R.id.textView)

        private var taskGroup: List<String>? = null
        private var isChecked: Boolean? = null

        var context: Context = itemView.context

        init {
            itemView.setOnClickListener(this)

            icon = VectorDrawableCompat.create(context.resources, R.drawable.ic_check_white_18dp, null)
            icon?.setColorFilter(ContextCompat.getColor(context, R.color.brand_100), PorterDuff.Mode.MULTIPLY)
        }

        fun bind(taskGroup: List<String>, isChecked: Boolean?) {
            this.taskGroup = taskGroup
            this.isChecked = isChecked

            taskGroup?.let {
                textView.text = it[0]
            }
            if (this.isChecked == true) {
                this.textView.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null)
                textView.background.setColorFilter(ContextCompat.getColor(context, R.color.white), PorterDuff.Mode.MULTIPLY)
                textView.setTextColor(ContextCompat.getColor(context, R.color.brand_100))
            } else {
                this.textView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                textView.background.setColorFilter(ContextCompat.getColor(context, R.color.brand_100), PorterDuff.Mode.MULTIPLY)
                textView.setTextColor(ContextCompat.getColor(context, R.color.white))
            }
        }

        override fun onClick(v: View) {
            val position = this.adapterPosition
            checkedList[position] = !checkedList[position]
            notifyItemChanged(position)
        }
    }

}
