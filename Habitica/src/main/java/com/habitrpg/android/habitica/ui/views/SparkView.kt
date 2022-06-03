package com.habitrpg.android.habitica.ui.views

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import androidx.core.content.ContextCompat
import com.habitrpg.android.habitica.R
import com.habitrpg.common.habitica.extensions.dpToPx
import kotlin.math.min

class SparkView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var spacing: Float = 0f
        set(value) {
            field = value
            invalidate()
        }
    private var paint: Paint = Paint()

    var thickness = 3.dpToPx(context)
    var length = 6.dpToPx(context)
    var maxSpacing = 5.dpToPx(context)
    var animationDuration = 2500L
    var color: Int
        get() {
            return paint.color
        }
        set(value) {
            paint.color = value
        }

    init {
        spacing = maxSpacing.toFloat()
        context.theme?.obtainStyledAttributes(attrs, R.styleable.SparkView, 0, 0)?.let {
            thickness = it.getDimensionPixelSize(R.styleable.SparkView_thickness, 3.dpToPx(context))
            length = it.getDimensionPixelSize(R.styleable.SparkView_length, 6.dpToPx(context))
            maxSpacing = it.getDimensionPixelSize(R.styleable.SparkView_maxSpacing, 5.dpToPx(context))
            animationDuration = it.getInt(R.styleable.SparkView_duration, 2500).toLong()
            color = it.getInt(R.styleable.SparkView_color, ContextCompat.getColor(context, R.color.white))
        }

        paint.style = Paint.Style.FILL
    }

    fun startAnimating() {
        val anim = ObjectAnimator.ofFloat(thickness.toFloat(), maxSpacing.toFloat())
        anim.addUpdateListener {
            spacing = it.animatedValue as Float
        }
        anim.interpolator = AccelerateDecelerateInterpolator()
        anim.repeatCount = Animation.INFINITE
        anim.repeatMode = ValueAnimator.REVERSE
        anim.duration = animationDuration
        anim.start()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val desiredSize = (length * 2 + maxSpacing)

        val width = when (MeasureSpec.getMode(widthMeasureSpec)) {
            MeasureSpec.EXACTLY -> widthSize
            MeasureSpec.AT_MOST -> min(desiredSize, widthSize)
            else -> desiredSize
        }

        val height = when (MeasureSpec.getMode(heightMeasureSpec)) {
            MeasureSpec.EXACTLY -> heightSize
            MeasureSpec.AT_MOST -> min(desiredSize, heightSize)
            else -> desiredSize
        }

        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        val thisCanvas = canvas ?: return
        val centerHorizontal = width / 2f
        val centerVertical = height / 2f
        val offset = (maxSpacing - spacing) / 2
        drawHorizontal(thisCanvas, offset, centerVertical)
        drawHorizontal(thisCanvas, width - length.toFloat() - offset, centerVertical)

        drawVertical(thisCanvas, centerHorizontal, offset)
        drawVertical(thisCanvas, centerVertical, height - length.toFloat() - offset)
    }

    private fun drawVertical(canvas: Canvas, x: Float, y: Float) {
        canvas.drawRoundRect(x - (thickness / 2), y, x + (thickness / 2), y + length, thickness / 2f, thickness / 2f, paint)
    }

    private fun drawHorizontal(canvas: Canvas, x: Float, y: Float) {
        canvas.drawRoundRect(x, y - (thickness / 2), x + length, y + (thickness / 2), thickness / 2f, thickness / 2f, paint)
    }
}
