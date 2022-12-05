package com.habitrpg.android.habitica.ui.views.equipment

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.ItemImageRowBinding
import com.habitrpg.common.habitica.extensions.layoutInflater
import com.habitrpg.common.habitica.extensions.loadImage

class EquipmentItemRow(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    private val binding: ItemImageRowBinding = ItemImageRowBinding.inflate(context.layoutInflater, this)
    var equipmentIdentifier: String? = null
        set(value) {
            field = value
            val imageName = if (equipmentIdentifier?.isNotEmpty() == true && equipmentIdentifier?.endsWith("base_0") == false) "shop_$equipmentIdentifier" else "icon_head_0"
            binding.imageView.loadImage(imageName)
        }

    var customizationIdentifier: String? = null
        set(value) {
            field = value
            val imageName = if (customizationIdentifier?.isNotEmpty() == true) "icon_$customizationIdentifier" else "icon_head_0"
            binding.imageView.loadImage(imageName)
        }

    init {
        View.inflate(context, R.layout.item_image_row, this)
        isClickable = true

        val attributes = context.theme?.obtainStyledAttributes(
            attrs,
            R.styleable.EquipmentItemRow,
            0, 0
        )

        binding.titleTextView.text = attributes?.getString(R.styleable.EquipmentItemRow_equipmentTitle)
        binding.valueTextView.visibility = View.GONE
    }
}
