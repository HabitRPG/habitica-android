package com.habitrpg.common.habitica.helpers

import android.view.View
import android.view.ViewAnimationUtils
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.Animation.INFINITE
import android.view.animation.Animation.RELATIVE_TO_SELF
import android.view.animation.Animation.REVERSE
import android.view.animation.AnimationSet
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.view.animation.TranslateAnimation
import androidx.core.animation.doOnEnd
import kotlin.math.hypot
import kotlin.random.Random

object Animations {
    private fun randomFloat(min: Float, max: Float): Float {
        return min + Random.nextFloat() * (max - min)
    }

    fun bobbingAnimation(amount: Float = 8f): Animation {
        val anim = TranslateAnimation(0f, 0f, -amount, amount)
        anim.duration = 2500
        anim.interpolator = AccelerateDecelerateInterpolator()
        anim.repeatCount = INFINITE
        anim.repeatMode = REVERSE
        return anim
    }

    fun negativeShakeAnimation(intensity: Float = 1f): Animation {
        val anim = AnimationSet(true)
        anim.interpolator = LinearInterpolator()

        val translate = TranslateAnimation(randomFloat(-2f * intensity, 0f), randomFloat(0f, 2f * intensity), randomFloat(-1f * intensity, 0f), randomFloat(0f, 1f * intensity))
        translate.duration = 70
        translate.repeatCount = 5
        translate.repeatMode = REVERSE
        anim.addAnimation(translate)

        val rotate = RotateAnimation(randomFloat(-0.4f * intensity, 0f), randomFloat(0f, 0.4f * intensity), RELATIVE_TO_SELF, 0.5f, RELATIVE_TO_SELF, 0.5f)
        rotate.duration = 70
        rotate.repeatCount = 5
        rotate.repeatMode = REVERSE
        anim.addAnimation(rotate)

        return anim
    }

    fun circularReveal(view: View, duration: Long = 300) {
        if (!view.isAttachedToWindow) return
        val cx = view.width / 2
        val cy = view.height / 2
        val finalRadius = hypot(cx.toDouble(), cy.toDouble()).toFloat()
        val anim = ViewAnimationUtils.createCircularReveal(view, cx, cy, 0f, finalRadius)
        anim.duration = duration
        anim.interpolator = AccelerateInterpolator()
        view.visibility = View.VISIBLE
        anim.start()
    }

    fun circularHide(view: View, duration: Long = 300) {
        val cx = view.width / 2
        val cy = view.height / 2
        val initialRadius = hypot(cx.toDouble(), cy.toDouble()).toFloat()
        val anim = ViewAnimationUtils.createCircularReveal(view, cx, cy, initialRadius, 0f)
        anim.duration = duration
        anim.interpolator = AccelerateInterpolator()
        anim.doOnEnd {
            view.visibility = View.INVISIBLE
        }
        anim.start()
    }

    fun fadeInAnimation(duration: Long = 300): Animation {
        val anim = AlphaAnimation(0f, 1f)
        anim.interpolator = AccelerateDecelerateInterpolator()
        anim.fillBefore = true
        anim.fillAfter = true
        anim.duration = duration
        return anim
    }

    fun fadeOutAnimation(): Animation {
        val anim = AlphaAnimation(1f, 0f)
        anim.interpolator = AccelerateDecelerateInterpolator()
        return anim
    }
}
