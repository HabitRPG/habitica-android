package com.habitrpg.android.habitica.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.ProgressBarBinding
import com.habitrpg.android.habitica.extensions.layoutInflater
import com.habitrpg.android.habitica.ui.helpers.DataBindingUtils
import kotlin.math.min


class HabiticaProgressBar(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {
    val binding = ProgressBarBinding.inflate(context.layoutInflater, this)

    var barForegroundColor: Int = 0
    set(value) {
        field = value
        DataBindingUtils.setRoundedBackground(binding.bar, value)
    }

    var barPendingColor: Int = 0
        set(value) {
            field = value
            DataBindingUtils.setRoundedBackground(binding.pendingBar, value)
        }

    var barBackgroundColor: Int = 0
    set(value) {
        field = value
        if (value != 0) {
            DataBindingUtils.setRoundedBackground(this, value)
        }
    }

    private var currentValue: Double = 0.0
    private var maxValue: Double = 0.0

    var pendingValue: Double = 0.0
        set(value) {
            if (field != value) {
                field = value
                updateBar()
            }
        }

    private fun updateBar() {
        val remainingValue = currentValue - pendingValue
        val remainingPercent = if (remainingValue < 0) {
            0.0
        } else {
            min(1.0, remainingValue / maxValue)
        }
        val pendingPercent = min(1.0, currentValue / maxValue)
        this.setBarWeight(remainingPercent)
        this.setPendingBarWeight(pendingPercent)
    }

    init {
        View.inflate(context, R.layout.progress_bar, this)

        val attributes = context.theme?.obtainStyledAttributes(
                attrs,
                R.styleable.HabiticaProgressBar,
                0, 0)

        barForegroundColor = attributes?.getColor(R.styleable.HabiticaProgressBar_barForegroundColor, 0) ?: 0
        barPendingColor = attributes?.getColor(R.styleable.HabiticaProgressBar_barPendingColor, 0) ?: 0
        barBackgroundColor = attributes?.getColor(R.styleable.HabiticaProgressBar_barBackgroundColor, 0) ?: 0
    }


    private fun setBarWeight(percent: Double) {
        setLayoutWeight(binding.bar, percent)
        setLayoutWeight(binding.emptyBarSpace, 1.0f - percent)
    }
    private fun setPendingBarWeight(percent: Double) {
        setLayoutWeight(binding.pendingBar, percent)
        setLayoutWeight(binding.pendingEmptyBarSpace, 1.0f - percent)
    }


    fun set(value: Double, valueMax: Double) {
        currentValue = value
        maxValue = valueMax
        updateBar()
    }

    fun setCurrentValue(value: Double) {
        currentValue = value
        updateBar()
    }

    fun setMaxValue(value: Double) {
        maxValue = value
        updateBar()
    }

    private fun setLayoutWeight(view: View, weight: Double) {
        view.clearAnimation()
        val layout = view.layoutParams as? LinearLayout.LayoutParams
        if (weight == 0.0 || weight == 1.0) {
            layout?.weight = weight.toFloat()
            view.layoutParams = layout
        } else if (layout?.weight?.toDouble() != weight) {
            val anim = DataBindingUtils.LayoutWeightAnimation(view, weight.toFloat())
            anim.duration = 300
            view.startAnimation(anim)
        }
    }
}