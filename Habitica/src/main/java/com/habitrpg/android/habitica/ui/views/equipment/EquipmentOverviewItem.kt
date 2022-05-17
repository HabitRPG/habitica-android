package com.habitrpg.android.habitica.ui.views.equipment

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.EquipmentOverviewItemBinding
import com.habitrpg.android.habitica.extensions.layoutInflater
import com.habitrpg.common.habitica.extensions.loadImage
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper

class EquipmentOverviewItem @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var binding: EquipmentOverviewItemBinding = EquipmentOverviewItemBinding.inflate(context.layoutInflater, this)

    init {
        if (attrs != null) {
            val styledAttrs = context.obtainStyledAttributes(attrs, R.styleable.EquipmentOverviewItem)
            binding.titleView.text = styledAttrs.getString(R.styleable.EquipmentOverviewItem_title)
            styledAttrs.recycle()
        }
        orientation = VERTICAL
    }

    var identifier: String = ""

    fun set(key: String?, isTwoHanded: Boolean = false, isDisabledFromTwoHand: Boolean = false) {
        identifier = key ?: ""
        binding.twoHandedIndicator.setImageDrawable(null)
        if (identifier.isNotEmpty() && !identifier.endsWith("base_0")) {
            binding.iconView.loadImage("shop_$key")
            binding.localIconView.visibility = View.GONE
            binding.iconView.visibility = View.VISIBLE
            binding.iconWrapper.background = ContextCompat.getDrawable(context, R.drawable.layout_rounded_bg_content)
            if (isTwoHanded) {
                binding.twoHandedIndicator.setImageDrawable(BitmapDrawable(context.resources, HabiticaIconsHelper.imageOfTwoHandedIcon()))
            }
        } else {
            binding.localIconView.visibility = View.VISIBLE
            binding.iconView.visibility = View.GONE
            if (isDisabledFromTwoHand) {
                binding.iconWrapper.background = ContextCompat.getDrawable(context, R.drawable.layout_rounded_bg_content)
                binding.localIconView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.equipment_two_handed))
            } else {
                binding.iconWrapper.background = ContextCompat.getDrawable(context, R.drawable.layout_rounded_bg_gray_10)
                binding.localIconView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.equipment_nothing_equipped))
            }
        }
    }
}
