package com.habitrpg.android.habitica.ui.views.equipment

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.EquipmentOverviewItemBinding
import com.habitrpg.android.habitica.extensions.layoutInflater
import com.habitrpg.android.habitica.ui.helpers.DataBindingUtils

class EquipmentOverviewItem @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
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

    fun set(key: String) {
        identifier = key
        if (key.isNotEmpty() && !key.endsWith("base_0")) {
            DataBindingUtils.loadImage(binding.iconView, "shop_$key")
            binding.noEquippedView.visibility = View.GONE
            binding.iconView.visibility = View.VISIBLE
            binding.iconWrapper.background = ContextCompat.getDrawable(context, R.drawable.layout_rounded_bg_white)
        } else {
            binding.noEquippedView.visibility = View.VISIBLE
            binding.iconView.visibility = View.GONE
            binding.iconWrapper.background = ContextCompat.getDrawable(context, R.drawable.layout_rounded_bg_gray_10)
        }
    }
}