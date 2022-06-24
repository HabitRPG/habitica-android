package com.habitrpg.wearos.habitica.ui.views

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import com.habitrpg.android.habitica.databinding.StatValueLayoutBinding
import com.habitrpg.common.habitica.extensions.layoutInflater
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.pow

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
                (truncateFormat(animation.animatedValue.toString().toLong()))
        }
        animator.start()
    }

    private fun truncateFormat(statCount: Long): String {
        //If we want to truncate and remove text after decimal (Example 1200 -> 1k)
        if (statCount < 1000) return "" + statCount
        return (statCount / 1000).toString() + "k";


        //If we want to truncate including text after decimal (Example 1200 -> 1.2k)
//        val exp = (ln(statCount.toDouble()) / ln(1000.0)).toInt()
//        binding.currentValue.letterSpacing = -.07f
//        return String.format("%.1f%c", statCount / 1000.0.pow(exp.toDouble()), "kMGTPE"[exp - 1])

    }

}