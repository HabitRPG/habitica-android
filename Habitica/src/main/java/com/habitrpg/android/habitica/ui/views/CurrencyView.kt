package com.habitrpg.android.habitica.ui.views

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import androidx.core.content.ContextCompat
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.helpers.NumberAbbreviator

class CurrencyView : androidx.appcompat.widget.AppCompatTextView {
    var lightBackground: Boolean = false
        set(value) {
            field = value
            configureCurrency()
        }
    var currency: String? = null
        set(currency) {
            field = currency
            configureCurrency()
            updateVisibility()
        }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        val attributes = context.theme?.obtainStyledAttributes(
                attrs,
                R.styleable.CurrencyViews,
                0, 0)
        lightBackground = try {
            attributes?.getBoolean(R.styleable.CurrencyView_hasLightBackground, true) ?: true
        } catch (_: ArrayIndexOutOfBoundsException) {
            true
        }
        visibility = GONE
    }

    constructor(context: Context, currency: String, lightbackground: Boolean) : super(context) {
        this.lightBackground = lightbackground
        this.currency = currency
        visibility = GONE
    }

    private fun configureCurrency() {
        if ("gold" == currency) {
            icon = HabiticaIconsHelper.imageOfGold()
            if (lightBackground) {
                setTextColor(ContextCompat.getColor(context, R.color.yellow_5))
            } else {
                setTextColor(ContextCompat.getColor(context, R.color.yellow_100))
            }
        } else if ("gems" == currency) {
            icon = HabiticaIconsHelper.imageOfGem()
            if (lightBackground) {
                setTextColor(ContextCompat.getColor(context, R.color.green_10))
            } else {
                setTextColor(ContextCompat.getColor(context, R.color.green_50))
            }
        } else if ("hourglasses" == currency) {
            icon = HabiticaIconsHelper.imageOfHourglass()
            if (lightBackground) {
                setTextColor(ContextCompat.getColor(context, R.color.brand_300))
            } else {
                setTextColor(ContextCompat.getColor(context, R.color.brand_500))
            }
        }
    }

    private var drawable: BitmapDrawable? = null

    var icon: Bitmap? = null
    set(value) {
        field = value
        if (value != null) {
            drawable = BitmapDrawable(resources, value)
            this.setCompoundDrawablesWithIntrinsicBounds(drawable,
                    null, null, null)
            val padding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    6f, context.resources.displayMetrics).toInt()
            compoundDrawablePadding = padding
            this.gravity = Gravity.CENTER_VERTICAL
        }
    }

    var value = 0.0
    set(value) {
        field = value
        text = NumberAbbreviator.abbreviate(context, value)
        updateVisibility()
    }

    var isLocked = false
    set(value) {
        field = value
        if (isLocked) {
            this.setTextColor(ContextCompat.getColor(context, R.color.gray_300))
            drawable?.alpha = 127
        } else {
            drawable?.alpha = 255
        }
        this.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
    }

    var cantAfford = false
    set(value) {
        field = value
        if (value) {
            this.setTextColor(ContextCompat.getColor(context, R.color.red_50))
            drawable?.alpha = 127
        } else {
            drawable?.alpha = 255
        }
        this.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
    }

    private fun updateVisibility() {
        visibility = if ("hourglasses" == this.currency) {
            if ("0" == text) View.GONE else View.VISIBLE
        } else {
            View.VISIBLE
        }
    }
}
