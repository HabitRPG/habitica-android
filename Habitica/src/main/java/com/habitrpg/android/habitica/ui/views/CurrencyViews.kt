package com.habitrpg.android.habitica.ui.views

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.widget.LinearLayout
import com.habitrpg.android.habitica.R

class CurrencyViews : LinearLayout {
    var lightBackground: Boolean = false
    set(value) {
        field = value
        hourglassTextView.lightBackground = value
        gemTextView.lightBackground = value
        goldTextView.lightBackground = value
    }
    private val hourglassTextView: CurrencyView = CurrencyView(context, "hourglasses", lightBackground)
    private val goldTextView: CurrencyView = CurrencyView(context, "gold", lightBackground)
    private val gemTextView: CurrencyView = CurrencyView(context, "gems", lightBackground)

    var gold: Double
        get() = goldTextView.value
        set(value) { goldTextView.value = value }
    var gems: Double
        get() = goldTextView.value
        set(value) { gemTextView.value = value }
    var hourglasses: Double
        get() = hourglassTextView.value
        set(value) { hourglassTextView.value = value }


    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        val attributes = context?.theme?.obtainStyledAttributes(
                attrs,
                R.styleable.CurrencyViews,
                0, 0)
        setupViews()
        lightBackground = attributes?.getBoolean(R.styleable.CurrencyViews_hasLightBackground, true) ?: true
    }

    constructor(context: Context?) : super(context) {
        setupViews()
    }

    private fun setupViews() {
        val margin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12f, context.resources.displayMetrics).toInt()
        setupView(hourglassTextView, margin)
        setupView(goldTextView, margin)
        setupView(gemTextView, margin)
    }

    private fun setupView(view: CurrencyView, margin: Int) {
        this.addView(view)
        val params = view.layoutParams as? LayoutParams
        params?.marginStart = margin
        view.layoutParams = params
    }
}
