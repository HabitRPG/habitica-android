package com.habitrpg.wearos.habitica.ui.views

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.SweepGradient
import android.util.AttributeSet
import android.view.View
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import com.habitrpg.android.habitica.R
import com.habitrpg.common.habitica.extensions.dpToPx
import kotlin.math.min

class IndeterminateProgressView
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
    ) : View(context, attrs, defStyleAttr) {
        private val progressBarWidth = 6f.dpToPx(context)
        private val revealDuration = 200L

        private val rainbow =
            listOf(
                ContextCompat.getColor(context, R.color.black),
                ContextCompat.getColor(context, R.color.watch_red_100),
                ContextCompat.getColor(context, R.color.watch_orange_100),
                ContextCompat.getColor(context, R.color.watch_yellow_100),
                ContextCompat.getColor(context, R.color.watch_green_100),
                ContextCompat.getColor(context, R.color.watch_blue_100),
                ContextCompat.getColor(context, R.color.watch_purple_100),
                ContextCompat.getColor(context, R.color.black),
            ).toIntArray()
        val gradient = SweepGradient(225f, 225f, rainbow, null)
        private val paint = Paint()
        private val isCircular: Boolean
        private val cornerRadius = 12f.dpToPx(context)

        private val currentAngle = 0f

        init {
            paint.style = Paint.Style.STROKE
            paint.shader = gradient
            paint.strokeWidth = 0f
            paint.isAntiAlias = true
            setWillNotDraw(false)
            isCircular = context.resources.configuration.isScreenRound
        }

        private var animator: ValueAnimator? = null

        fun startAnimation() {
            animator =
                ValueAnimator.ofFloat(0f, 359f).apply {
                    duration = 2000
                    repeatCount = Animation.INFINITE
                    repeatMode = ValueAnimator.RESTART
                    interpolator = LinearInterpolator()
                    addUpdateListener {
                        val matrix = Matrix()
                        matrix.postRotate(it.animatedValue as Float, 225f, 225f)
                        gradient.setLocalMatrix(matrix)
                        invalidate()
                        requestLayout()
                    }
                    start()
                }
            ValueAnimator.ofFloat(0f, progressBarWidth).apply {
                duration = revealDuration
                addUpdateListener { paint.strokeWidth = it.animatedValue as Float }
                start()
            }
        }

        fun stopAnimation(onAnimationEnd: () -> Unit) {
            val hideDuration = min(revealDuration, animator?.currentPlayTime ?: revealDuration)
            ValueAnimator.ofFloat(paint.strokeWidth, 0f).apply {
                duration = hideDuration
                addUpdateListener { paint.strokeWidth = it.animatedValue as Float }
                start()
            }.doOnEnd {
                animator = null
                onAnimationEnd()
            }
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val halfBar = paint.strokeWidth / 2f
            if (isCircular) {
                canvas.drawArc(halfBar, halfBar, width.toFloat() - halfBar, height.toFloat() - halfBar, currentAngle, 360f, false, paint)
            } else {
                canvas.drawRoundRect(halfBar, halfBar, width.toFloat() - halfBar, height.toFloat() - halfBar, cornerRadius, cornerRadius, paint)
            }
        }
    }
