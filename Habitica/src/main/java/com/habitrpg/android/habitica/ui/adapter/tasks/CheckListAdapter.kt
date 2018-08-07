package com.habitrpg.android.habitica.ui.adapter.tasks

import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.models.tasks.ChecklistItem
import com.habitrpg.android.habitica.ui.helpers.ItemTouchHelperAdapter
import com.habitrpg.android.habitica.ui.helpers.ItemTouchHelperViewHolder
import com.habitrpg.android.habitica.ui.helpers.bindView
import net.pherth.android.emoji_library.EmojiEditText
import java.util.*

class CheckListAdapter : RecyclerView.Adapter<CheckListAdapter.ItemViewHolder>(), ItemTouchHelperAdapter {

    private val items = ArrayList<ChecklistItem>()

    val checkListItems: List<ChecklistItem>
        get() = items

    fun setItems(items: List<ChecklistItem>) {
        this.items.addAll(items)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.checklist_item, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.textWatcher.id = null
        holder.checkListTextView.setText(items[position].text)
        holder.textWatcher.id = items[position].id
    }

    fun addItem(item: ChecklistItem) {
        items.add(item)
        notifyItemInserted(items.size - 1)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onItemDismiss(position: Int) {
        if (position >= 0 && position < items.size) {
            items.removeAt(position)
            notifyItemRemoved(position)
        }
    }


    override fun onItemMove(fromPosition: Int, toPosition: Int) {
        val item = items[fromPosition]
        items.removeAt(fromPosition)
        items.add(toPosition, item)
        notifyItemMoved(fromPosition, toPosition)
    }

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), ItemTouchHelperViewHolder, View.OnClickListener {

        internal val textWatcher: ChecklistTextWatcher = ChecklistTextWatcher()
        internal val checkListTextView: EmojiEditText by bindView(itemView, R.id.item_edittext)
        private val deleteButton: Button by bindView(itemView, R.id.delete_item_button)

        init {
            deleteButton.setOnClickListener(this)
            checkListTextView.addTextChangedListener(textWatcher)
        }

        override fun onItemSelected() {
            itemView.setBackgroundColor(Color.LTGRAY)
        }

        override fun onItemClear() {
            itemView.setBackgroundColor(0)
        }

        override fun onClick(v: View) {
            if (v === deleteButton) {
                this@CheckListAdapter.onItemDismiss(adapterPosition)
            }
        }

        internal inner class ChecklistTextWatcher : TextWatcher {

            var id: String? = null

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) {
                if (id == null) {
                    return
                }
                for (item in items) {
                    if (id == item.id) {
                        item.text = checkListTextView.text.toString()
                        break
                    }
                }
            }
        }
    }
}