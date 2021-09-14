package com.habitrpg.android.habitica.ui

import android.content.Context
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.WindowManager
import android.widget.LinearLayout
import com.habitrpg.android.habitica.R
import kotlin.math.min

class MaxHeightLinearLayout : LinearLayout {

    private val defaultHeight = 0.9f
    private var maxHeight: Float = 0.toFloat()
    private val displaymetrics = DisplayMetrics()

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        if (!isInEditMode) {
            init(context, attrs)
        }
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        if (!isInEditMode) {
            init(context, attrs)
        }
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        if (!isInEditMode) {
            init(context, attrs)
        }
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        if (attrs != null) {
            val styledAttrs = context.obtainStyledAttributes(attrs, R.styleable.MaxHeightLinearLayout)
            // 200 is a defualt value
            maxHeight = styledAttrs.getFloat(R.styleable.MaxHeightLinearLayout_maxHeightMultiplier, defaultHeight)

            styledAttrs.recycle()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var heightMeasurement = heightMeasureSpec

        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
        windowManager?.defaultDisplay?.getMetrics(displaymetrics)
        val height = (displaymetrics.heightPixels * maxHeight).toInt()
        heightMeasurement = min(heightMeasurement, MeasureSpec.makeMeasureSpec(height, MeasureSpec.AT_MOST))

        super.onMeasure(widthMeasureSpec, heightMeasurement)
    }
}
