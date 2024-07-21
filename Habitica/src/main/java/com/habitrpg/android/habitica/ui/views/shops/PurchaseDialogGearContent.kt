package com.habitrpg.android.habitica.ui.views.shops

import android.content.Context
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.DialogPurchaseContentGearBinding
import com.habitrpg.android.habitica.models.inventory.Equipment
import com.habitrpg.android.habitica.models.shops.ShopItem
import com.habitrpg.common.habitica.extensionsCommon.layoutInflater
import com.habitrpg.common.habitica.viewsCommon.PixelArtView

internal class PurchaseDialogGearContent(context: Context) : PurchaseDialogContent(context) {
    val binding = DialogPurchaseContentGearBinding.inflate(context.layoutInflater, this)
    override val imageView: PixelArtView
        get() = binding.imageView
    override val titleTextView: TextView
        get() = binding.titleTextView

    override fun setItem(item: ShopItem) {
        super.setItem(item)
        binding.notesTextView.text = item.notes
    }

    fun setEquipment(equipment: Equipment) {
        if (equipment.isValid && equipment.type != "mystery") {
            configureFieldsForValue(binding.strLabel, binding.strValue, equipment.str)
            configureFieldsForValue(binding.perLabel, binding.perValue, equipment.per)
            configureFieldsForValue(binding.conLabel, binding.conValue, equipment.con)
            configureFieldsForValue(binding.intLabel, binding.intValue, equipment.intelligence)
        } else {
            hideStatsTable()
        }
    }

    fun hideStatsTable() {
        binding.statsTable.isVisible = false
    }

    private fun configureFieldsForValue(
        labelView: TextView?,
        valueTextView: TextView?,
        value: Int,
    ) {
        valueTextView?.text = "+$value"
        if (value == 0) {
            labelView?.setTextColor(ContextCompat.getColor(context, R.color.text_dimmed))
            valueTextView?.setTextColor(ContextCompat.getColor(context, R.color.text_dimmed))
        }
    }
}
