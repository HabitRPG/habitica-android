package com.habitrpg.android.habitica.ui.views.login

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper

class StarView : AppCompatImageView {
    private var blinkDurations: List<Int>? = null
    private var blinkIndex = 0
    private var currentAnimator: ObjectAnimator? = null
    private var isDetached = false

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context) : super(context) {
        this.scaleType = ScaleType.CENTER
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isDetached = false
    }

    override fun onDetachedFromWindow() {
        isDetached = true
        currentAnimator?.cancel()
        currentAnimator = null
        super.onDetachedFromWindow()
    }

    fun setStarSize(size: Int) {
        when (size) {
            0 -> {
                this.setImageBitmap(HabiticaIconsHelper.imageOfStarSmall())
            }

            1 -> {
                this.setImageBitmap(HabiticaIconsHelper.imageOfStarMedium())
            }

            2 -> {
                this.setImageBitmap(HabiticaIconsHelper.imageOfStarLarge())
            }
        }
    }

    fun setBlinkDurations(blinkDurations: List<Int>) {
        this.blinkDurations = blinkDurations
        runBlink()
    }

    private fun runBlink() {
        if (isDetached) return
        if (blinkIndex >= (blinkDurations?.size ?: 0)) {
            blinkIndex = 0
        }
        val animator = ObjectAnimator.ofFloat(this, View.ALPHA, 0f)
        animator.duration = 1000
        animator.startDelay = blinkDurations?.get(blinkIndex)?.toLong() ?: 0
        animator.repeatCount = 1
        animator.repeatMode = ValueAnimator.REVERSE
        animator.addListener(
            object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    if (isDetached) return
                    blinkIndex++
                    runBlink()
                }
            },
        )
        currentAnimator = animator
        try {
            animator.start()
        } catch (ignored: NullPointerException) {
        }
    }
}
