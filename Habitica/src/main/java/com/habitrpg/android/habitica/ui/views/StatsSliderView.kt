package com.habitrpg.android.habitica.ui.views

import android.content.Context
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.SeekBar
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.extensions.styledAttributes
import kotlinx.android.synthetic.main.stats_slider_view.view.*

class StatsSliderView(context: Context?, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    var previousValue = 0
    set(value) {
        field = value
        previousValueTextView.text = value.toString()
    }

    var maxValue = 0
    set(value) {
        field = value
        statsSeekBar.max = field
    }

    var currentValue = 0
    set(value) {
        field = value
        statsSeekBar.progress = value
        valueEditText.setText(value.toString())
    }

    var allocateAction: ((Int) -> Unit)? = null

    init {
        View.inflate(context, R.layout.stats_slider_view, this)
        gravity = Gravity.CENTER_VERTICAL

        val attributes = attrs?.styledAttributes(context, R.styleable.StatsSliderView)

        if (attributes != null) {
            statTypeTitle.text = attributes.getString(R.styleable.StatsSliderView_statsTitle)
            val statColor = attributes.getColor(R.styleable.StatsSliderView_statsColor, 0)
            statTypeTitle.setTextColor(attributes.getColor(R.styleable.StatsSliderView_statsTextColor, 0))
            statsSeekBar.progressDrawable.setColorFilter(statColor, PorterDuff.Mode.MULTIPLY)
            statsSeekBar.thumb.setColorFilter(statColor, PorterDuff.Mode.MULTIPLY)
        }

        statsSeekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                currentValue = progress
                if (fromUser) {
                    allocateAction?.invoke(currentValue)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })

        currentValue = 0
    }

}

