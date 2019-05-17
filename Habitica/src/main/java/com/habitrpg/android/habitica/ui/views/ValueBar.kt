package com.habitrpg.android.habitica.ui.views

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.extensions.getThemeColor
import com.habitrpg.android.habitica.ui.helpers.bindView
import java.math.RoundingMode
import java.text.NumberFormat


class ValueBar(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {

    private val iconView: ImageView by bindView(R.id.ic_header)
    private val secondaryIconView: ImageView by bindView(R.id.secondaryIconView)
    private val descriptionIconView: ImageView by bindView(R.id.descriptionIconView)
    private val valueTextView: TextView by bindView(R.id.valueLabel)
    private val descriptionTextView: TextView by bindView(R.id.descriptionLabel)
    private val progressBar: HabiticaProgressBar by bindView(R.id.progressBar)
    private val labelWrapper: ViewGroup by bindView(R.id.labelWrapper)

    private val formatter = NumberFormat.getInstance()

    private var currentValue: Double = 0.0
    set(value) {
        field = value
        updateBar()
    }

    var pendingValue: Double = 0.0
        set(value) {
            field = value
            updateBar()
        }

    private var maxValue: Double = 0.0
        set(value) {
            field = value
            updateBar()
        }

    var barHeight: Int? = null
    set(value) {
        field = value
        if (value != null) {
            progressBar.layoutParams.height = value
        }
    }

    var description: String = ""
    set(value) {
        field = value
        descriptionTextView.text = description
    }

    private fun updateBar() {
        this.progressBar.pendingValue = pendingValue
        this.progressBar.currentValue = currentValue
        this.progressBar.maxValue = maxValue
        this.setValueText(formatter.format(currentValue) + " / " + formatter.format(maxValue.toInt()))
    }

    init {
        View.inflate(context, R.layout.value_bar, this)

        val attributes = context.theme?.obtainStyledAttributes(
                attrs,
                R.styleable.ValueBar,
                0, 0)
        setLightBackground(attributes?.getBoolean(R.styleable.ValueBar_lightBackground, false) == true)

        progressBar.barForegroundColor = attributes?.getColor(R.styleable.ValueBar_barForegroundColor, 0) ?: 0
        progressBar.barPendingColor = attributes?.getColor(R.styleable.ValueBar_barPendingColor, 0) ?: 0
        progressBar.barBackgroundColor = attributes?.getColor(R.styleable.ValueBar_barBackgroundColor, 0) ?: 0

        val labelSpacing = attributes?.getDimension(R.styleable.ValueBar_labelSpacing, context.resources.getDimension(R.dimen.spacing_small))
        if (labelSpacing != null) {
            labelWrapper.setPadding(0, labelSpacing.toInt(), 0, 0)
        }

        barHeight = attributes?.getDimension(R.styleable.ValueBar_barHeight, context.resources.getDimension(R.dimen.bar_size))?.toInt()

        val valueTextColor = attributes?.getColor(R.styleable.ValueBar_valueTextColor, 0) ?: 0
        if (valueTextColor != 0) {
            valueTextView.setTextColor(valueTextColor)
        }
        val descriptionTextColor = attributes?.getColor(R.styleable.ValueBar_descriptionTextColor, 0) ?: 0
        if (descriptionTextColor != 0) {
            descriptionTextView.setTextColor(descriptionTextColor)
        }

        val iconRes = attributes?.getDrawable(R.styleable.ValueBar_barIconDrawable)
        if (iconRes != null) {
            setIcon(iconRes)
        }

        descriptionTextView.text = attributes?.getString(R.styleable.ValueBar_description)

        formatter.maximumFractionDigits = 1
        formatter.roundingMode = RoundingMode.UP
        formatter.isGroupingUsed = true
    }

    fun setIcon(iconRes: Drawable) {
        iconView.setImageDrawable(iconRes)
        iconView.visibility = View.VISIBLE
    }

    fun setIcon(bitmap: Bitmap) {
        iconView.setImageBitmap(bitmap)
        iconView.visibility = View.VISIBLE
    }

    fun setSecondaryIcon(iconRes: Drawable) {
        secondaryIconView.setImageDrawable(iconRes)
        secondaryIconView.visibility = View.VISIBLE
    }

    fun setSecondaryIcon(bitmap: Bitmap) {
        secondaryIconView.setImageBitmap(bitmap)
        secondaryIconView.visibility = View.VISIBLE
    }

    fun setDescriptionIcon(iconRes: Drawable) {
        descriptionIconView.setImageDrawable(iconRes)
        descriptionIconView.visibility = View.VISIBLE
    }

    fun setDescriptionIcon(bitmap: Bitmap) {
        descriptionIconView.setImageBitmap(bitmap)
        descriptionIconView.visibility = View.VISIBLE
    }

    fun setValueText(valueText: String) {
        valueTextView.text = valueText
    }

    fun setLightBackground(lightBackground: Boolean) {
        val textColor: Int
        if (lightBackground) {
            textColor = ContextCompat.getColor(context, R.color.gray_10)
            progressBar.setBackgroundResource(R.drawable.layout_rounded_bg_light_gray)
        } else {
            textColor = context.getThemeColor(R.attr.textColorPrimaryDark)
            progressBar.setBackgroundResource(R.drawable.layout_rounded_bg_primary)
        }
        valueTextView.setTextColor(textColor)
        descriptionTextView.setTextColor(textColor)
    }

    fun set(value: Double, valueMax: Double) {
        currentValue = value
        maxValue = valueMax
    }

    fun setLabelVisibility(visibility: Int) {
        valueTextView.visibility = visibility
        descriptionTextView.visibility = visibility
    }
}
