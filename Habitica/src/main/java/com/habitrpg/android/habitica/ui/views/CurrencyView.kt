package com.habitrpg.android.habitica.ui.views

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.content.ContextCompat
import androidx.core.view.updateLayoutParams
import com.habitrpg.common.habitica.R
import com.habitrpg.common.habitica.extensions.isUsingNightModeResources
import com.habitrpg.common.habitica.helpers.NumberAbbreviator

class CurrencyView : androidx.appcompat.widget.AppCompatTextView {
    var hideWhenEmpty: Boolean = false
    var lightBackground: Boolean = false
        set(value) {
            field = value
            configureCurrency()
        }
    var currency: String? = null
        set(currency) {
            field = currency
            setCurrencyContentDescriptionFromCurrency(currency)
            configureCurrency()
            updateVisibility()
        }
    private var currencyContentDescription: String? = null

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        val attributes = context.theme?.obtainStyledAttributes(
            attrs,
            R.styleable.CurrencyView,
            0,
            0
        )
        val fallBackLight = !context.isUsingNightModeResources()
        lightBackground = try {
            attributes?.getBoolean(R.styleable.CurrencyView_hasLightBackground, fallBackLight) ?: fallBackLight
        } catch (_: ArrayIndexOutOfBoundsException) {
            !context.isUsingNightModeResources()
        }
        currency = attributes?.getString(R.styleable.CurrencyView_currency)
        visibility = GONE
        setSingleLine()
    }

    constructor(context: Context, currency: String, lightbackground: Boolean) : super(context) {
        this.lightBackground = lightbackground
        this.currency = currency
        setCurrencyContentDescriptionFromCurrency(currency)
        visibility = GONE
        setSingleLine()
    }

    private fun setCurrencyContentDescriptionFromCurrency(currency: String?) {
        when (currency) {
            "gold" -> this.currencyContentDescription = context.getString(R.string.gold_plural)
            "gems" -> this.currencyContentDescription = context.getString(R.string.gems)
            "hourglasses" -> this.currencyContentDescription = context.getString(R.string.mystic_hourglasses)
            else -> this.currencyContentDescription = ""
        }
    }

    private fun configureCurrency() {
        if ("gold" == currency) {
            icon = HabiticaIconsHelper.imageOfGold()
            if (lightBackground) {
                setTextColor(ContextCompat.getColor(context, R.color.yellow_1))
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
        hideWhenEmpty = "hourglasses" == currency
    }

    private var drawable: BitmapDrawable? = null

    var icon: Bitmap? = null
        set(value) {
            field = value
            if (value != null) {
                drawable = BitmapDrawable(resources, value)
                this.setCompoundDrawablesWithIntrinsicBounds(
                    drawable,
                    null,
                    null,
                    null
                )
                val padding = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    6f,
                    context.resources.displayMetrics
                ).toInt()
                compoundDrawablePadding = padding
                this.gravity = Gravity.CENTER_VERTICAL
            }
        }

    var minForAbbrevation = 0
    var decimals = 2
    var animationDuration = 500L
    var animationDelay = 0L

    private fun update(value: Double) {
        text = NumberAbbreviator.abbreviate(context, value, decimals, minForAbbrevation = minForAbbrevation)
    }

    private fun endUpdate() {
        contentDescription = "$text $currencyContentDescription"
        updateVisibility()
        updateLayoutParams {
            width = ViewGroup.LayoutParams.WRAP_CONTENT
        }
    }

    var value = 0.0
        set(value) {
            if (text.isEmpty() || animationDuration == 0L) {
                update(value)
                endUpdate()
            } else {
                val animator = ValueAnimator.ofFloat(field.toFloat(), value.toFloat())
                animator.duration = animationDuration
                animator.startDelay = animationDelay
                animator.doOnStart {
                    layoutParams.width = width
                }
                animator.addUpdateListener {
                    update((it.animatedValue as Float).toDouble())
                }
                animator.doOnEnd {
                    endUpdate()
                }
                animator.start()
            }
            field = value
        }

    var isLocked = false
        set(value) {
            field = value
            if (isLocked) {
                this.setTextColor(ContextCompat.getColor(context, R.color.text_quad))
                drawable?.alpha = 127
            } else {
                drawable?.alpha = 255
            }
            this.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
        }

    private fun updateVisibility() {
        visibility = if (hideWhenEmpty) {
            if ("0" == text) View.GONE else View.VISIBLE
        } else {
            View.VISIBLE
        }
    }
}
