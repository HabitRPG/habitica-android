package com.habitrpg.wearos.habitica.ui.views

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import com.habitrpg.android.habitica.databinding.StatValueLayoutBinding
import com.habitrpg.common.habitica.extensions.layoutInflater

class StatValue @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) :
    ConstraintLayout(
        context,
        attrs,
        defStyle
    ) {

    var binding = StatValueLayoutBinding.inflate(context.layoutInflater, this)

    fun setStatValue(maxValue: Int, currentValue: Int, bitmap: Bitmap, bitmapColor: Int) {
        binding.bitmap.setImageBitmap(bitmap)
        binding.currentValue.text = currentValue.toString()
        binding.currentValue.setTextColor(
            context?.resources?.getColor(bitmapColor, null) ?: Color.WHITE
        )
        binding.maxValue.text = "/$maxValue"
        invalidate()
    }


}