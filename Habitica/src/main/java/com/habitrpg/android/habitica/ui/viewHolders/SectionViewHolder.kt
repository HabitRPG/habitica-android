package com.habitrpg.android.habitica.ui.viewHolders

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.ui.helpers.bindView

class SectionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val label: TextView by bindView(itemView, R.id.label)
    private val purchaseSetButton: Button? by bindView(itemView, R.id.purchaseSetButton)
    private val selectionSpinner: Spinner? by bindView(itemView, R.id.classSelectionSpinner)
    internal val notesView: TextView? by bindView(itemView, R.id.headerNotesView)
    var context: Context = itemView.context

    var spinnerSelectionChanged: (() -> Unit)? = null

    init {
        this.purchaseSetButton?.visibility = View.GONE
        selectionSpinner?.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                spinnerSelectionChanged?.invoke()
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                spinnerSelectionChanged?.invoke()
            }
        }

    }

    fun bind(title: String) {
        try {
            val stringID = context.resources.getIdentifier("section$title", "string", context.packageName)
            this.label.text = context.getString(stringID)
        } catch (e: Exception) {
            this.label.text = title
        }
    }

    var spinnerAdapter: ArrayAdapter<CharSequence>? = null
    set(value) {
        field = value
        selectionSpinner?.adapter = field
        selectionSpinner?.visibility = if (value != null) View.VISIBLE else View.GONE
    }

    var selectedItem: Int = 0
        get() = selectionSpinner?.selectedItemPosition ?: 0
    set(value) {
        field = value
        selectionSpinner?.setSelection(field)
    }
}
