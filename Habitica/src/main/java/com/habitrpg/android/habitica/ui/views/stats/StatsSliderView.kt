package com.habitrpg.android.habitica.ui.views.stats

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.SeekBar
import androidx.core.content.ContextCompat
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.StatsSliderViewBinding
import com.habitrpg.android.habitica.extensions.AfterChangeTextWatcher
import com.habitrpg.android.habitica.extensions.setTintWith
import com.habitrpg.android.habitica.extensions.styledAttributes
import com.habitrpg.common.habitica.extensions.layoutInflater

class StatsSliderView(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {
    private val binding = StatsSliderViewBinding.inflate(context.layoutInflater, this)

    var previousValue = 0
        set(value) {
            field = value
            binding.previousValueTextView.text = value.toString()
        }

    var maxValue = 0
        set(value) {
            field = value
            binding.statsSeekBar.max = field
        }

    var currentValue = 0
        set(value) {
            field = value
            binding.statsSeekBar.progress = value
            binding.valueEditText.setText(value.toString())
            if (binding.valueEditText.isFocused) {
                binding.valueEditText.setSelection(binding.valueEditText.length())
            }
        }

    var allocateAction: ((Int) -> Unit)? = null

    init {
        gravity = Gravity.CENTER_VERTICAL

        val attributes = attrs?.styledAttributes(context, R.styleable.StatsSliderView)

        if (attributes != null) {
            binding.statTypeTitle.text = attributes.getString(R.styleable.StatsSliderView_statsTitle)
            val statColor = attributes.getColor(R.styleable.StatsSliderView_statsColor, 0)
            binding.statTypeTitle.setTextColor(attributes.getColor(R.styleable.StatsSliderView_statsTextColor, 0))
            binding.statsSeekBar.progressTintList = ColorStateList.valueOf(statColor)
            val thumbDrawable = ContextCompat.getDrawable(context, R.drawable.seekbar_thumb)
            thumbDrawable?.setTintWith(statColor, PorterDuff.Mode.MULTIPLY)
            binding.statsSeekBar.thumb = thumbDrawable
        }

        binding.valueEditText.addTextChangedListener(
            AfterChangeTextWatcher { s ->
                val newValue = try {
                    s.toString().toInt()
                } catch (e: NumberFormatException) {
                    0
                }
                if (newValue != currentValue && newValue <= maxValue && newValue > 0) {
                    currentValue = newValue
                    allocateAction?.invoke(currentValue)
                } else if (newValue > maxValue || newValue < 0) {
                    binding.valueEditText.setText(currentValue.toString())
                    binding.valueEditText.setSelection(binding.valueEditText.length())
                }
            }
        )

        binding.statsSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                currentValue = progress
                if (fromUser) {
                    allocateAction?.invoke(currentValue)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) { /* no-on */ }

            override fun onStopTrackingTouch(seekBar: SeekBar?) { /* no-on */ }
        })

        currentValue = 0
    }
}
