package com.habitrpg.common.habitica.views

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.habitrpg.android.habitica.extensions.dpToPx
import com.habitrpg.android.habitica.extensions.getThemeColor
import com.habitrpg.android.habitica.extensions.isUsingNightModeResources
import com.habitrpg.android.habitica.extensions.layoutInflater
import com.habitrpg.common.habitica.R
import com.habitrpg.common.habitica.databinding.ValueBarBinding
import java.math.RoundingMode
import java.text.NumberFormat

class ValueBar(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {

    var descriptionIconVisibility: Int
        get() = binding.descriptionIconView.visibility
        set(value) {
            binding.descriptionIconView.visibility = value
        }

    private var binding: ValueBarBinding = ValueBarBinding.inflate(context.layoutInflater, this, true)
    private val formatter = NumberFormat.getInstance()

    val progressBar: HabiticaProgressBar
        get() = binding.progressBar

    var currentValue: Double
        get() = binding.progressBar.currentValue
        set(value) {
            binding.progressBar.currentValue = value
        }
    var maxValue: Double
        get() = binding.progressBar.maxValue
        set(value) {
            binding.progressBar.maxValue = value
        }

    var barForegroundColor: Int
        get() = binding.progressBar.barForegroundColor
        set(value) {
            binding.progressBar.barForegroundColor = value
        }

    var barPendingColor: Int
        get() = binding.progressBar.barPendingColor
        set(value) {
            binding.progressBar.barPendingColor = value
        }

    var barBackgroundColor: Int
        get() = binding.progressBar.barBackgroundColor
        set(value) {
            binding.progressBar.barBackgroundColor = value
        }

    var pendingValue: Double = 0.0
        set(value) {
            if (field != value) {
                field = value
                updateBar()
            }
        }

    var barHeight: Int? = null
        set(value) {
            field = value
            if (value != null) {
                binding.progressBar.layoutParams.height = value
            }
        }

    var description: String = ""
        set(value) {
            field = value
            binding.descriptionTextView.text = description
        }

    private fun updateBar() {
        binding.progressBar.set(currentValue, maxValue)
        binding.progressBar.pendingValue = pendingValue
        setValueText(formatter.format(currentValue) + " / " + formatter.format(maxValue.toInt()))
    }

    init {

        val attributes = context.theme?.obtainStyledAttributes(
            attrs,
            R.styleable.ValueBar,
            0, 0
        )
        setLightBackground(attributes?.getBoolean(R.styleable.ValueBar_lightBackground, !context.isUsingNightModeResources()) == true)

        binding.progressBar.barForegroundColor = attributes?.getColor(R.styleable.ValueBar_barForegroundColor, 0) ?: 0
        binding.progressBar.barPendingColor = attributes?.getColor(R.styleable.ValueBar_barPendingColor, 0) ?: 0
        binding.progressBar.barBackgroundColor = attributes?.getColor(R.styleable.ValueBar_barBackgroundColor, 0) ?: 0

        val labelSpacing = attributes?.getDimension(R.styleable.ValueBar_labelSpacing, 2.dpToPx(context).toFloat())
        if (labelSpacing != null) {
            binding.labelWrapper.setPadding(0, labelSpacing.toInt(), 0, 0)
        }

        barHeight = attributes?.getDimension(R.styleable.ValueBar_barHeight, context.resources.getDimension(R.dimen.bar_size))?.toInt()

        val valueTextColor = attributes?.getColor(R.styleable.ValueBar_valueTextColor, 0) ?: 0
        if (valueTextColor != 0) {
            binding.valueTextView.setTextColor(valueTextColor)
        }
        val descriptionTextColor = attributes?.getColor(R.styleable.ValueBar_descriptionTextColor, 0) ?: 0
        if (descriptionTextColor != 0) {
            binding.descriptionTextView.setTextColor(descriptionTextColor)
        }

        val iconRes = attributes?.getDrawable(R.styleable.ValueBar_barIconDrawable)
        if (iconRes != null) {
            setIcon(iconRes)
        }

        binding.descriptionTextView.text = attributes?.getString(R.styleable.ValueBar_description)

        formatter.maximumFractionDigits = 1
        formatter.roundingMode = RoundingMode.UP
        formatter.isGroupingUsed = true
    }

    fun setIcon(iconRes: Drawable) {
        binding.iconView.setImageDrawable(iconRes)
        binding.iconView.visibility = View.VISIBLE
    }

    fun setIcon(bitmap: Bitmap) {
        binding.iconView.setImageBitmap(bitmap)
        binding.iconView.visibility = View.VISIBLE
    }

    fun setSecondaryIcon(iconRes: Drawable) {
        binding.secondaryIconView.setImageDrawable(iconRes)
        binding.secondaryIconView.visibility = View.VISIBLE
    }

    fun setSecondaryIcon(bitmap: Bitmap) {
        binding.secondaryIconView.setImageBitmap(bitmap)
        binding.secondaryIconView.visibility = View.VISIBLE
    }

    fun setDescriptionIcon(iconRes: Drawable) {
        binding.descriptionIconView.setImageDrawable(iconRes)
        binding.descriptionIconView.visibility = View.VISIBLE
    }

    fun setDescriptionIcon(bitmap: Bitmap) {
        binding.descriptionIconView.setImageBitmap(bitmap)
        binding.descriptionIconView.visibility = View.VISIBLE
    }

    fun setValueText(valueText: String) {
        binding.valueTextView.text = valueText
    }

    fun setLightBackground(lightBackground: Boolean) {
        val textColor: Int
        /*if (lightBackground) {
            textColor = ContextCompat.getColor(context, R.color.text_ternary)
            binding.progressBar.setBackgroundResource(R.drawable.layout_rounded_bg_light_gray)
        } else {
            textColor = context.getThemeColor(R.attr.textColorPrimaryDark)
            binding.progressBar.setBackgroundResource(R.drawable.layout_rounded_bg_header_bar)
        }
        binding.valueTextView.setTextColor(textColor)
        binding.descriptionTextView.setTextColor(textColor)*/
    }

    var animationDuration = 500L
    var animationDelay = 0L

    fun set(value: Double, valueMax: Double) {
        if (binding.progressBar.currentValue != value || maxValue != valueMax) {
            if (animationDuration == 0L || binding.valueTextView.text.isEmpty()) {
                currentValue = value
            } else {
                val animator = ValueAnimator.ofInt(currentValue.toInt(), value.toInt())
                animator.duration = animationDuration
                animator.startDelay = animationDelay
                animator.addUpdateListener {
                    currentValue = (it.animatedValue as Int).toDouble()
                    updateBar()
                }
                animator.start()
            }
            maxValue = valueMax
            updateBar()
        }
    }

    fun setLabelVisibility(visibility: Int) {
        binding.valueTextView.visibility = visibility
        binding.descriptionTextView.visibility = visibility
    }
}
