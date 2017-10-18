package com.habitrpg.android.habitica.ui.views

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.helpers.NumberAbbreviator

class CurrencyView : android.support.v7.widget.AppCompatTextView {

    private var lightbackground = false
    var currency: String? = null
        set(currency) {
            field = currency
            if ("gold" == currency) {
                setIcon(HabiticaIconsHelper.imageOfGold())
                if (lightbackground) {
                    setTextColor(ContextCompat.getColor(context, R.color.yellow_5))
                } else {
                    setTextColor(ContextCompat.getColor(context, R.color.yellow_100))
                }
            } else if ("gems" == currency) {
                setIcon(HabiticaIconsHelper.imageOfGem())
                if (lightbackground) {
                    setTextColor(ContextCompat.getColor(context, R.color.green_100))
                } else {
                    setTextColor(ContextCompat.getColor(context, R.color.green_50))
                }
            } else if ("hourglasses" == currency) {
                setIcon(HabiticaIconsHelper.imageOfHourglass())
                if (lightbackground) {
                    setTextColor(ContextCompat.getColor(context, R.color.brand_300))
                } else {
                    setTextColor(ContextCompat.getColor(context, R.color.brand_500))
                }
            }
            updateVisibility()
        }
    private var drawable: BitmapDrawable? = null

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}

    constructor(context: Context, currency: String, lightbackground: Boolean) : super(context) {
        this.lightbackground = lightbackground
        this.currency = currency
    }

    private fun setIcon(iconBitmap: Bitmap) {
        drawable = BitmapDrawable(resources, iconBitmap)
        this.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
        val padding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6f, context.resources.displayMetrics).toInt()
        compoundDrawablePadding = padding
        this.gravity = Gravity.CENTER_VERTICAL
    }

    private fun updateVisibility() {
        if ("hourglasses" == this.currency) {
            visibility = if ("0" == text) View.GONE else View.VISIBLE
        }
    }

    fun setValue(value: Double?) {
        text = NumberAbbreviator.abbreviate(context, value!!)
        updateVisibility()
    }

    fun setLocked(isLocked: Boolean) {
        if (drawable == null) {
            return
        }
        if (isLocked) {
            this.setTextColor(ContextCompat.getColor(context, R.color.gray_300))
            drawable!!.alpha = 127
        } else {
            drawable!!.alpha = 255
        }

        this.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
    }

    fun setCantAfford(cantAfford: Boolean) {
        if (drawable == null) {
            return
        }
        if (cantAfford) {
            this.setTextColor(ContextCompat.getColor(context, R.color.red_50))
            drawable!!.alpha = 127
        } else {
            drawable!!.alpha = 255
        }

        this.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
    }
}
