package com.habitrpg.wearos.habitica.ui.views

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import com.habitrpg.android.habitica.databinding.StatValueLayoutBinding
import com.habitrpg.common.habitica.extensions.layoutInflater
import com.habitrpg.common.habitica.helpers.NumberAbbreviator

class StatValue @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) :
    ConstraintLayout(
        context,
        attrs,
        defStyle
    ) {

    var binding = StatValueLayoutBinding.inflate(context.layoutInflater, this)

    fun setStatValues(maxValue: Int, currentValue: Int) {
        binding.currentValue.text = currentValue.toString()
        binding.maxValue.text = "/$maxValue"
        invalidate()

        startUpdateCountAnimation(currentValue)
    }

    fun setStatValueResources(bitmap: Bitmap, bitmapColor: Int) {
        binding.bitmap.setImageBitmap(bitmap)
        binding.currentValue.setTextColor(
            context?.resources?.getColor(bitmapColor, null) ?: Color.WHITE
        )
    }

    private fun startUpdateCountAnimation(statValue: Int) {
        val animator = ValueAnimator.ofInt(0, statValue)
        animator.duration = 1000
        animator.addUpdateListener { animation ->
            binding.currentValue.text =
                (NumberAbbreviator.abbreviate(context, animation.animatedValue.toString().toDouble(), 0))
        }
        animator.start()
    }

}