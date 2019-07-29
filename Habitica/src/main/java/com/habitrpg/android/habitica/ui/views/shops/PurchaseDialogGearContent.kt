package com.habitrpg.android.habitica.ui.views.shops

import android.content.Context
import androidx.core.content.ContextCompat
import android.util.AttributeSet
import android.widget.TextView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.ui.helpers.bindView
import com.habitrpg.android.habitica.models.inventory.Equipment
import com.habitrpg.android.habitica.models.shops.ShopItem

internal class PurchaseDialogGearContent : PurchaseDialogContent {

    private val notesTextView: TextView by bindView(R.id.notesTextView)
    private val strLabel: TextView by bindView(R.id.str_label)
    private val strValueTextView: TextView by bindView(R.id.str_value)
    private val perLabel: TextView by bindView(R.id.per_label)
    private val perValueTextView: TextView by bindView(R.id.per_value)
    private val conLabel: TextView by bindView(R.id.con_label)
    private val conValueTextView: TextView by bindView(R.id.con_value)
    private val intLabel: TextView by bindView(R.id.int_label)
    private val intValueTextView: TextView by bindView(R.id.int_value)

    override val viewId: Int
        get() = R.layout.dialog_purchase_content_gear

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    override fun setItem(item: ShopItem) {
        super.setItem(item)
        notesTextView.text = item.notes
    }

    fun setEquipment(equipment: Equipment) {
        if (equipment.isValid) {
            configureFieldsForValue(strLabel, strValueTextView, equipment.str)
            configureFieldsForValue(perLabel, perValueTextView, equipment.per)
            configureFieldsForValue(conLabel, conValueTextView, equipment.con)
            configureFieldsForValue(intLabel, intValueTextView, equipment._int)
        } else {
            configureFieldsForValue(strLabel, strValueTextView, 0)
            configureFieldsForValue(perLabel, perValueTextView, 0)
            configureFieldsForValue(conLabel, conValueTextView, 0)
            configureFieldsForValue(intLabel, intValueTextView, 0)
        }
    }

    private fun configureFieldsForValue(labelView: TextView?, valueTextView: TextView?, value: Int) {
        valueTextView?.text = "+$value"
        if (value == 0) {
            labelView?.setTextColor(ContextCompat.getColor(context, R.color.gray_400))
            valueTextView?.setTextColor(ContextCompat.getColor(context, R.color.gray_400))
        }
    }
}
