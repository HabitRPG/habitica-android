package com.habitrpg.wearos.habitica.ui.views

import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import com.habitrpg.android.habitica.R


class CircularProgressView(
    context: Context?,
    attrs: AttributeSet?
) : View(context, attrs) {
    private val ovalSpace = RectF()
    private val parentArcColor = context?.resources?.getColor(R.color.inverted_background, null) ?: Color.GRAY
    private val fillArcColor = context?.resources?.getColor(R.color.blue_500, null) ?: Color.BLUE
    private var ovalSize = 100
    private var currentPercentage = 55
    private var PERCENTAGE_DIVIDER = 180
    private val ARC_FULL_ROTATION_DEGREE = 360


    override fun onDraw(canvas: Canvas?) {
        setSpace()
        canvas?.let {
            // 2
            drawBackgroundArc(it)
            // 3
            drawInnerArc(it)
        }
    }

    fun setCircularProgressView(ovalSize: Int) {
        this.ovalSize = ovalSize
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

    private val parentArcPaint = Paint().apply {
        style = Paint.Style.STROKE
        isAntiAlias = true
        color = parentArcColor
        strokeWidth = 15f
    }

    private val fillArcPaint = Paint().apply {
        style = Paint.Style.STROKE
        isAntiAlias = true
        color = fillArcColor
        strokeWidth = 15f
        // 1
        strokeCap = Paint.Cap.ROUND
    }

    private fun drawBackgroundArc(it: Canvas) {
        it.drawArc(ovalSpace, 0f, 360f, false, parentArcPaint)
    }

    private fun drawInnerArc(canvas: Canvas) {
        val percentageToFill = getCurrentPercentageToFill()
        canvas.drawArc(ovalSpace, 270f, percentageToFill, false, fillArcPaint)
    }

    fun animateProgress() {
        // 1
        val valuesHolder = PropertyValuesHolder.ofFloat(
            PERCENTAGE_VALUE_HOLDER,
            0f,
            100f
        )

        // 2
        val animator = ValueAnimator().apply {
            setValues(valuesHolder)
            duration = 1000
            interpolator = AccelerateDecelerateInterpolator()

            // 3
            addUpdateListener {
                // 4
                val percentage = it.getAnimatedValue(PERCENTAGE_VALUE_HOLDER) as Float

                // 5
                currentPercentage = percentage.toInt()

                // 6
                invalidate()
            }
        }
        // 7
        animator.start()
    }
    companion object {
        // ...
        const val PERCENTAGE_VALUE_HOLDER = "percentage"
    }

    private fun getCurrentPercentageToFill() = 280f
//        (ARC_FULL_ROTATION_DEGREE * (currentPercentage / PERCENTAGE_DIVIDER)).toFloat()
}

