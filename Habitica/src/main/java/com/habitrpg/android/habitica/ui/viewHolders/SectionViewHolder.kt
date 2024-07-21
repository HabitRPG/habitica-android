package com.habitrpg.android.habitica.ui.viewHolders

import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.extensions.getImpreciseRemainingString
import com.habitrpg.android.habitica.extensions.getTranslatedAnimalType
import com.habitrpg.common.habitica.extensionsCommon.inflate
import com.habitrpg.android.habitica.models.inventory.StableSection
import com.habitrpg.android.habitica.ui.views.CurrencyView
import java.util.Date

class SectionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val headerContainer: LinearLayout? = itemView.findViewById(R.id.header_container)
    private val label: TextView = itemView.findViewById(R.id.label)
    private val switchesInLabel: TextView? = itemView.findViewById(R.id.switches_in_label)
    private val selectionSpinner: Spinner? = itemView.findViewById(R.id.class_selection_spinner)
    val switchClassButton: LinearLayout? = itemView.findViewById(R.id.change_class_button)
    val switchClassLabel: TextView? = itemView.findViewById(R.id.change_class_label)
    val switchClassDescription: TextView? = itemView.findViewById(R.id.change_class_description)
    val switchClassCurrency: CurrencyView? = itemView.findViewById(R.id.change_class_currency_view)
    internal val notesView: TextView? = itemView.findViewById(R.id.headerNotesView)
    private val countPill: TextView? = itemView.findViewById(R.id.count_pill)
    val divider: View? = itemView.findViewById(R.id.divider)
    val classSelectionButton: FrameLayout? = itemView.findViewById(R.id.class_selection_button)
    val classSelectionLabel: TextView? = itemView.findViewById(R.id.class_name_label)
    var context: Context = itemView.context

    var spinnerSelectionChanged: (() -> Unit)? = null

    constructor(parent: ViewGroup) : this(parent.inflate(R.layout.customization_section_header))

    init {
        classSelectionButton?.setOnClickListener {
            selectionSpinner?.performClick()
        }
        selectionSpinner?.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {
                    spinnerSelectionChanged?.invoke()
                }

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long,
                ) {
                    spinnerSelectionChanged?.invoke()
                }
            }
    }

    fun bind(title: String) {
        try {
            val stringID =
                context.resources.getIdentifier("section$title", "string", context.packageName)
            this.label.text = context.getString(stringID).uppercase()
        } catch (e: Exception) {
            this.label.text = title.uppercase()
        }
    }

    fun bind(section: StableSection) {
        label.text =
            if (section.type == "pets") {
                context.getString(R.string.pet_category, getTranslatedAnimalType(context, section.key))
            } else {
                context.getString(
                    R.string.mount_category,
                    getTranslatedAnimalType(context, section.key),
                )
            }
        if (section.key == "special") {
            countPill?.visibility = View.GONE
        } else {
            countPill?.visibility = View.VISIBLE
        }
        label.gravity = Gravity.START
        countPill?.text =
            itemView.context.getString(
                R.string.pet_ownership_fraction,
                section.ownedCount,
                section.totalCount,
            )
    }

    fun bind(endDate: Date?) {
        if (endDate != null) {
            switchesInLabel?.visibility = View.VISIBLE
            if (endDate.time < Date().time) {
                switchesInLabel?.text = context.getString(R.string.tap_to_reload)
            } else {
                switchesInLabel?.text = context.getString(R.string.switches_in_x, endDate.getImpreciseRemainingString(context.resources))
            }
        } else {
            switchesInLabel?.visibility = View.GONE
        }
    }

    fun setSelectedClass(selectedGearCategory: String) {
        var textColor = R.color.white
        when (selectedGearCategory) {
            "warrior" -> {
                classSelectionButton?.background = AppCompatResources.getDrawable(context, R.drawable.shop_header_warrior_background)
                classSelectionLabel?.text = context.getString(R.string.warrior)
            }
            "wizard" -> {
                classSelectionButton?.background = AppCompatResources.getDrawable(context, R.drawable.shop_header_wizard_background)
                classSelectionLabel?.text = context.getString(R.string.mage)
            }
            "healer" -> {
                classSelectionButton?.background = AppCompatResources.getDrawable(context, R.drawable.shop_header_healer_background)
                classSelectionLabel?.text = context.getString(R.string.healer)
                textColor = R.color.darkbrown
            }
            "rogue" -> {
                classSelectionButton?.background = AppCompatResources.getDrawable(context, R.drawable.shop_header_rogue_background)
                classSelectionLabel?.text = context.getString(R.string.rogue)
            }
            else -> {
                classSelectionButton?.background = AppCompatResources.getDrawable(context, R.drawable.shop_header_else_background)
                classSelectionLabel?.text = context.getString(R.string.classless)
                textColor = R.color.gray_100
            }
        }
        classSelectionLabel?.setTextColor(ContextCompat.getColor(context, textColor))
    }

    var spinnerAdapter: ArrayAdapter<CharSequence>? = null
        set(value) {
            field = value
            selectionSpinner?.adapter = field
            classSelectionButton?.visibility = if (value != null) View.VISIBLE else View.GONE
        }

    var selectedItem: Int = 0
        get() = selectionSpinner?.selectedItemPosition ?: 0
        set(value) {
            field = value
            selectionSpinner?.setSelection(field)
        }
}
