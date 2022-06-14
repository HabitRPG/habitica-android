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
    private val parentArcColor = context?.resources?.getColor(R.color.bar_background_color, null) ?: Color.GRAY
    var fillArcColor = context?.resources?.getColor(R.color.hp_bar_color, null) ?: parentArcColor
    var ovalSize = 200
    private var currentPercentage = 55
    private var PERCENTAGE_DIVIDER = 180
    private val ARC_FULL_ROTATION_DEGREE = 360

    private val parentArcPaint = Paint().apply {
        style = Paint.Style.STROKE
        isAntiAlias = true
        color = parentArcColor
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
        var percentageToFill = getCurrentPercentageToFill()
        canvas.drawArc(ovalSpace, 270f, percentageToFill, false, fillArcPaint)
    }

    fun setBarColor(barColor: Int) {
        fillArcColor = context?.resources?.getColor(barColor, null) ?: parentArcColor
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

    private fun getCurrentPercentageToFill() = if(currentPercentage > 0) {(ARC_FULL_ROTATION_DEGREE.toFloat() * (currentPercentage.toFloat() / PERCENTAGE_DIVIDER.toFloat()))} else {1f}
}

