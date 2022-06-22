package com.habitrpg.wearos.habitica.ui.views

import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import com.habitrpg.android.habitica.R


class CircularProgressView(
    context: Context?,
    attrs: AttributeSet?
) : View(context, attrs) {
    private val ovalSpace = RectF()
    private var ovalSize = (resources.displayMetrics.heightPixels / 2)
    private var currentPercentage = 55
    private var PERCENTAGE_DIVIDER = 180
    private val ARC_FULL_ROTATION_DEGREE = 360
    val attributes = context?.theme?.obtainStyledAttributes(
        attrs,
        R.styleable.CircularProgressView,
        0, 0
    )
    private val offset = attributes?.getInt(R.styleable.CircularProgressView_offset, 0)
    private val backgroundArcColor = attributes?.getColor(R.styleable.CircularProgressView_backgroundArcColor, 0) ?: Color.GRAY
    private var fillArcColor = attributes?.getColor(R.styleable.CircularProgressView_arcFillColor, 0) ?: Color.GRAY


    private val parentArcPaint = Paint().apply {
        style = Paint.Style.STROKE
        isAntiAlias = true
        color = backgroundArcColor
        strokeWidth = 10f
    }

    private var fillArcPaint = Paint().apply {
        style = Paint.Style.STROKE
        isAntiAlias = true
        color = fillArcColor
        strokeWidth = 10f
        strokeCap = Paint.Cap.ROUND
    }

    override fun onDraw(canvas: Canvas?) {
        setSpace()
        canvas?.let {
            drawBackgroundArc(it)
            drawInnerArc(it)
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        offset?.let { ovalSize = (height / 2) - it }
        invalidate()
    }

    private fun setSpace() {
        val horizontalCenter = (width.div(2)).toFloat()
        val verticalCenter = (height.div(2)).toFloat()
        val ovalSize = this.ovalSize
        ovalSpace.set(
            horizontalCenter - ovalSize,
            verticalCenter - ovalSize,
            horizontalCenter + ovalSize,
            verticalCenter + ovalSize
        )
    }


    private fun drawBackgroundArc(it: Canvas) {
        it.drawArc(ovalSpace, 0f, 360f, false, parentArcPaint)
    }

    private fun drawInnerArc(canvas: Canvas) {
        val percentageToFill = getCurrentAngleToFill()
        canvas.drawArc(ovalSpace, 270f, percentageToFill, false, fillArcPaint)
    }

    fun setBarColor(barColor: Int) {
        fillArcColor = context?.resources?.getColor(barColor, null) ?: backgroundArcColor
        fillArcPaint = Paint().apply {
            style = Paint.Style.STROKE
            isAntiAlias = true
            color = fillArcColor
            strokeWidth = 10f
            strokeCap = Paint.Cap.ROUND
        }
    }

    fun setPercentageValues(currentValue: Int, maxValue: Int) {
        currentPercentage = currentValue
        PERCENTAGE_DIVIDER = maxValue
    }

    fun animateProgress() {
        val currentPercent: Int = currentPercentage
        val valuesHolder = PropertyValuesHolder.ofFloat(
            PERCENTAGE_VALUE_HOLDER,
            1f,
            currentPercent.toFloat()
        )

        val animator = ValueAnimator().apply {
            setValues(valuesHolder)
            duration = 1000
            interpolator = AccelerateDecelerateInterpolator()

            addUpdateListener {
                val percentage = it.getAnimatedValue(PERCENTAGE_VALUE_HOLDER) as Float
                currentPercentage = percentage.toInt()
                invalidate()
            }
        }
        animator.start()
    }

    companion object {
        const val PERCENTAGE_VALUE_HOLDER = "percentage"
    }

    private fun getCurrentAngleToFill() = if(currentPercentage > 0) {(ARC_FULL_ROTATION_DEGREE.toFloat() * (currentPercentage.toFloat() / PERCENTAGE_DIVIDER.toFloat()))} else {1f}
}

