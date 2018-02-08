package com.habitrpg.android.habitica.ui.views

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.extensions.bindView
import com.habitrpg.android.habitica.ui.helpers.DataBindingUtils

class ValueBar(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {

    private val iconView: ImageView by bindView(R.id.ic_header)
    private val valueTextView: TextView by bindView(R.id.valueLabel)
    private val descriptionTextView: TextView by bindView(R.id.descriptionLabel)
    private val barBackground: ViewGroup by bindView(R.id.bar_full)
    private val barView: View by bindView(R.id.bar)
    private val barEmptySpace: View by bindView(R.id.empty_bar_space)

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
        this.setValueText(currentValue.toInt().toString() + "/" + maxValue.toInt())
    }

    init {
        View.inflate(context, R.layout.value_bar, this)

        val attributes = context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.ValueBar,
                0, 0)
        setLightBackground(attributes.getBoolean(R.styleable.ValueBar_lightBackground, false))

        val color = attributes.getColor(R.styleable.ValueBar_barForegroundColor, 0)
        DataBindingUtils.setRoundedBackground(barView, color)
        val backgroundColor = attributes.getColor(R.styleable.ValueBar_barBackgroundColor, 0)
        if (backgroundColor != 0) {
            DataBindingUtils.setRoundedBackground(barBackground, backgroundColor)
        }

        val textColor = attributes.getColor(R.styleable.ValueBar_textColor, 0)
        if (textColor != 0) {
            valueTextView.setTextColor(textColor)
            descriptionTextView.setTextColor(textColor)
        }

        val iconRes = attributes.getDrawable(R.styleable.ValueBar_barIconDrawable)
        if (iconRes != null) {
            setIcon(iconRes)
        }

        descriptionTextView.text = attributes.getString(R.styleable.ValueBar_description)
    }

    fun setIcon(iconRes: Drawable) {
        iconView.setImageDrawable(iconRes)
        iconView.visibility = View.VISIBLE
    }

    fun setIcon(bitmap: Bitmap) {
        iconView.setImageBitmap(bitmap)
        iconView.visibility = View.VISIBLE
    }

    private fun setBarWeight(percent: Double) {
        setLayoutWeight(barView, percent)
        setLayoutWeight(barEmptySpace, 1.0f - percent)
    }

    fun setValueText(valueText: String) {
        valueTextView.text = valueText
    }

    fun setLightBackground(lightBackground: Boolean) {
        val textColor: Int
        if (lightBackground) {
            textColor = ContextCompat.getColor(context, R.color.gray_10)
            barBackground.setBackgroundResource(R.drawable.layout_rounded_bg_light_gray)
        } else {
            textColor = ContextCompat.getColor(context, R.color.brand_500)
            barBackground.setBackgroundResource(R.drawable.layout_rounded_bg_brand)
        }
        valueTextView.setTextColor(textColor)
        descriptionTextView.setTextColor(textColor)
    }

    fun set(value: Double, valueMax: Double) {
        currentValue = value
        maxValue = valueMax
    }

    companion object {

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

    fun setLabelVisibility(visibility: Int) {
        valueTextView.visibility = visibility
        descriptionTextView.visibility = visibility
    }
}
