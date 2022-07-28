package com.habitrpg.wearos.habitica.ui.views

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.widget.RelativeLayout
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
        binding.chipImageview.setImageDrawable(attributes?.getDrawable(R.styleable.TextActionChip_chipImage))
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