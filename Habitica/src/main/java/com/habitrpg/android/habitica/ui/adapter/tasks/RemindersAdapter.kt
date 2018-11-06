package com.habitrpg.android.habitica.ui.adapter.tasks

import android.graphics.Color
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.helpers.RemindersManager
import com.habitrpg.android.habitica.models.tasks.RemindersItem
import com.habitrpg.android.habitica.ui.helpers.ItemTouchHelperAdapter
import com.habitrpg.android.habitica.ui.helpers.ItemTouchHelperViewHolder
import com.habitrpg.android.habitica.ui.helpers.bindView
import java.util.*

/**
 * Created by keithholliday on 5/31/16.
 */
class RemindersAdapter(private val taskType: String) : androidx.recyclerview.widget.RecyclerView.Adapter<RemindersAdapter.ItemViewHolder>(), ItemTouchHelperAdapter, RemindersManager.ReminderTimeSelectedCallback {

    private val reminders = ArrayList<RemindersItem>()
    private val remindersManager: RemindersManager = RemindersManager(taskType)

    val remindersItems: List<RemindersItem>
        get() = reminders

    fun setReminders(reminders: List<RemindersItem>) {
        this.reminders.addAll(reminders)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.reminder_item, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val time = reminders[position].time
        holder.reminderItemTextView.setText(remindersManager.reminderTimeToString(time))
        @Suppress("DEPRECATION")
        holder.hour = time?.hours ?: 0
        @Suppress("DEPRECATION")
        holder.minute = time?.minutes ?: 0
    }

    fun addItem(item: RemindersItem) {
        reminders.add(item)
        notifyItemInserted(reminders.size - 1)
    }

    override fun getItemCount(): Int {
        return reminders.size
    }


    override fun onItemDismiss(position: Int) {
        if (position >= 0 && position < reminders.size) {
            reminders.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int) {
        Collections.swap(reminders, fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
    }

    override fun onReminderTimeSelected(remindersItem: RemindersItem?) {
        if (remindersItem == null) {
            return
        }
        for (pos in reminders.indices) {
            if (remindersItem.id == reminders[pos].id) {
                reminders[pos] = remindersItem
                notifyItemChanged(pos)
                break
            }
        }
    }

    inner class ItemViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView), ItemTouchHelperViewHolder, View.OnClickListener {

        internal val reminderItemTextView: EditText by bindView(itemView, R.id.item_edittext)
        private val deleteButton: Button by bindView(itemView, R.id.delete_item_button)

        var hour: Int = 0
        var minute: Int = 0

        init {
            deleteButton.setOnClickListener(this)

            reminderItemTextView.setOnClickListener { v ->
                val reminder = reminders[adapterPosition]

                remindersManager.createReminderTimeDialog(this@RemindersAdapter, taskType, v.context, reminder)
            }
        }

        override fun onItemSelected() {
            itemView.setBackgroundColor(Color.LTGRAY)
        }

        override fun onItemClear() {
            itemView.setBackgroundColor(0)
        }

        override fun onClick(v: View) {
            if (v === deleteButton) {
                this@RemindersAdapter.onItemDismiss(adapterPosition)
            }
        }
    }
}
