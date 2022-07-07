package com.habitrpg.wearos.habitica.ui.views

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.TextActionChipBinding
import com.habitrpg.common.habitica.extensions.layoutInflater

class TextActionChipView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    RelativeLayout(context, attrs) {
    private val attributes = context.theme?.obtainStyledAttributes(
        attrs,
        R.styleable.TextActionChip,
        0, 0
    )

    val binding = TextActionChipBinding.inflate(context.layoutInflater, this)

    init {
        val chipText = attributes?.getText(R.styleable.TextActionChip_chipText)
        val chipImage = attributes?.getDrawable(R.styleable.TextActionChip_chipImage)

        binding.chipTextview.text = chipText
        binding.chipImageview.setImageDrawable(chipImage)
    }
}