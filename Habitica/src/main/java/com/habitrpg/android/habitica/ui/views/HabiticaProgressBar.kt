package com.habitrpg.android.habitica.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.extensions.bindView
import com.habitrpg.android.habitica.ui.helpers.DataBindingUtils


class HabiticaProgressBar(context: Context?, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    private val barView: View by bindView(R.id.bar)
    private val barEmptySpace: View by bindView(R.id.empty_bar_space)

    var barForegroundColor: Int = 0
    set(value) {
        field = value
        DataBindingUtils.setRoundedBackground(barView, value)
    }

    var barBackgroundColor: Int = 0
    set(value) {
        field = value
        if (value != 0) {
            DataBindingUtils.setRoundedBackground(this, value)
        }
    }

    var currentValue: Double = 0.0
        set(value) {
            field = value
            updateBar()
        }

    var maxValue: Double = 0.0
        set(value) {
            field = value
            updateBar()
        }

    private fun updateBar() {
        val percent = Math.min(1.0, currentValue / maxValue)

        this.setBarWeight(percent)
    }

    init {
        View.inflate(context, R.layout.progress_bar, this)

        val attributes = context?.theme?.obtainStyledAttributes(
                attrs,
                R.styleable.HabiticaProgressBar,
                0, 0)

        barForegroundColor = attributes?.getColor(R.styleable.HabiticaProgressBar_barForegroundColor, 0) ?: 0
        barBackgroundColor = attributes?.getColor(R.styleable.HabiticaProgressBar_barBackgroundColor, 0) ?: 0

    }


    private fun setBarWeight(percent: Double) {
        setLayoutWeight(barView, percent)
        setLayoutWeight(barEmptySpace, 1.0f - percent)
    }


    fun set(value: Double, valueMax: Double) {
        currentValue = value
        maxValue = valueMax
    }

    private fun setLayoutWeight(view: View?, weight: Double) {
        view!!.clearAnimation()
        val layout = view.layoutParams as LinearLayout.LayoutParams
        if (weight == 0.0 || weight == 1.0) {
            layout.weight = weight.toFloat()
            view.layoutParams = layout
        } else if (layout.weight.toDouble() != weight) {
            val anim = DataBindingUtils.LayoutWeightAnimation(view, weight.toFloat())
            anim.duration = 1250
            view.startAnimation(anim)
        }
    }
}