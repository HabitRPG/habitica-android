package com.habitrpg.wearos.habitica.ui.views

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.TaskRewardChipBinding
import com.habitrpg.android.habitica.extensions.round
import com.habitrpg.android.habitica.extensions.setScaledPadding
import com.habitrpg.common.habitica.extensions.layoutInflater
import java.math.RoundingMode
import java.text.NumberFormat

class TaskRewardChip @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {
    enum class Size {
        SMALL,
        MEDIUM,
        LARGE
    }

    val binding = TaskRewardChipBinding.inflate(context.layoutInflater, this)

    var size: Size = Size.MEDIUM
    set(value) {
        field = value
        binding.textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, when (field) {
            Size.SMALL -> 14f
            Size.MEDIUM -> 16f
            Size.LARGE -> 20f
        })
        when (field) {
            Size.SMALL -> setScaledPadding(context, 10, 9, 10, 9)
            Size.MEDIUM -> setScaledPadding(context, 17, 9, 17, 9)
            Size.LARGE -> setScaledPadding(context, 21, 12, 21, 12)
        }
    }

    init {
        background = ContextCompat.getDrawable(context, R.drawable.row_background)
        gravity = Gravity.CENTER
    }

    fun set(value: Double?, icon: Bitmap) {
        binding.iconView.setImageBitmap(icon)
        var text = formatter.format(value?.round(if (value < 1 && value > -1) 1 else 0))
        if (text.firstOrNull() == '0') {
            text = text.substring(1)
        }
        binding.textView.text = text
        backgroundTintList = if ((value ?: 0.0) > 0.0) {
            ColorStateList.valueOf(ContextCompat.getColor(context, R.color.watch_green_100))
        } else {
            ColorStateList.valueOf(ContextCompat.getColor(context, R.color.watch_red_100))
        }
    }

    companion object {
        private val formatter = NumberFormat.getInstance().apply {
            maximumFractionDigits = 1
            roundingMode = RoundingMode.UP
            isGroupingUsed = true
        }
    }
}