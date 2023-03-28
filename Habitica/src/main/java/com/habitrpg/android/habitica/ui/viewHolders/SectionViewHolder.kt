package com.habitrpg.android.habitica.ui.viewHolders

import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.extensions.getTranslatedAnimalType
import com.habitrpg.android.habitica.extensions.inflate
import com.habitrpg.android.habitica.models.inventory.StableSection
import com.habitrpg.android.habitica.ui.views.CurrencyView

class SectionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val label: TextView = itemView.findViewById(R.id.label)
    private val selectionSpinner: Spinner? = itemView.findViewById(R.id.classSelectionSpinner)
    val switchClassButton: LinearLayout? = itemView.findViewById(R.id.change_class_button)
    val switchClassLabel: TextView? = itemView.findViewById(R.id.change_class_label)
    val switchClassCurrency: CurrencyView? = itemView.findViewById(R.id.change_class_currency_view)
    internal val notesView: TextView? = itemView.findViewById(R.id.headerNotesView)
    private val countPill: TextView? = itemView.findViewById(R.id.count_pill)
    var context: Context = itemView.context

    var spinnerSelectionChanged: (() -> Unit)? = null

    constructor(parent: ViewGroup) : this(parent.inflate(R.layout.customization_section_header))

    init {
        selectionSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                spinnerSelectionChanged?.invoke()
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
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

    fun bind(section: StableSection) {
        label.text = if (section.type == "pets") {
            context.getString(R.string.pet_category, getTranslatedAnimalType(context, section.key))
        } else {
            context.getString(R.string.mount_category, getTranslatedAnimalType(context, section.key))
        }
        if (section.key == "special") {
            countPill?.visibility = View.GONE
        } else {
            countPill?.visibility = View.VISIBLE
        }
        label.gravity = Gravity.START
        countPill?.text = itemView.context.getString(R.string.pet_ownership_fraction, section.ownedCount, section.totalCount)
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
