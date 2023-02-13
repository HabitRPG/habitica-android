package com.habitrpg.wearos.habitica.ui.views

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.Gravity
import android.widget.RelativeLayout
import androidx.core.view.isVisible
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.TextActionChipBinding
import com.habitrpg.common.habitica.extensions.layoutInflater

open class TextActionChipView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    RelativeLayout(context, attrs) {
    private val attributes = context.theme?.obtainStyledAttributes(
        attrs,
        R.styleable.TextActionChip,
        0, 0
    )

    val binding = TextActionChipBinding.inflate(context.layoutInflater, this)

    init {
        binding.chipTextview.text = attributes?.getText(R.styleable.TextActionChip_chipText)
        val icon = attributes?.getDrawable(R.styleable.TextActionChip_chipImage)
        if (icon != null) {
            binding.chipImageview.setImageDrawable(icon)
            binding.chipTextview.gravity = Gravity.START
            binding.chipImageview.isVisible = true
        } else {
            binding.chipImageview.setImageDrawable(icon)
            binding.chipTextview.gravity = Gravity.START
            binding.chipImageview.isVisible = false
        }
        attributes?.getColor(R.styleable.TextActionChip_chipColor, context.getColor(R.color.surface))?.let {
            binding.wearChipButton.backgroundTintList = ColorStateList.valueOf(it)
        }
        attributes?.getColor(R.styleable.TextActionChip_chipTextColor, context.getColor(R.color.watch_white))?.let {
            binding.chipTextview.setTextColor(it)
        }
    }

    fun setChipText(text: String) {
        binding.chipTextview.text = text
    }
}
