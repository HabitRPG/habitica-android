package com.habitrpg.android.habitica.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.ui.AvatarView
import com.habitrpg.android.habitica.ui.helpers.DataBindingUtils
import kotlinx.android.synthetic.main.item_image_row.view.*

class EquipmentItemRow(context: Context?, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    var equipmentIdentifier: String? = null
    set(value) {
        field = value
        val imageName = if (equipmentIdentifier?.isNotEmpty() == true && equipmentIdentifier?.endsWith("base_0") == false) "shop_"+equipmentIdentifier else "head_0"
        DataBindingUtils.loadImage(imageView, imageName)
    }

    var customizationIdentifier: String? = null
    set(value) {
        field = value
        val imageName = if (customizationIdentifier?.isNotEmpty() == true) customizationIdentifier else "head_0"
        DataBindingUtils.loadImage(imageView, imageName)
    }

    var equipmentName: String? = ""
    set(value) {
        field = value
        valueTextView.text = equipmentName
    }

    init {
        View.inflate(context, R.layout.item_image_row, this)
        isClickable = true

        val attributes = context?.theme?.obtainStyledAttributes(
                attrs,
                R.styleable.EquipmentItemRow,
                0, 0)

        titleTextView.text = attributes?.getString(R.styleable.EquipmentItemRow_equipmentTitle)
    }
}
