package com.habitrpg.common.habitica.extensionsCommon

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.ViewTreeObserver

fun View.setScaledPadding(
    context: Context?,
    left: Int,
    top: Int,
    right: Int,
    bottom: Int,
) {
    this.setPadding(left.dpToPx(context), top.dpToPx(context), right.dpToPx(context), bottom.dpToPx(context))
}

inline fun View.waitForLayout(crossinline f: View.() -> Unit) =
    with(viewTreeObserver) {
        addOnGlobalLayoutListener(
            object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    viewTreeObserver.removeOnGlobalLayoutListener(this)
                    f()
                }
            },
        )
    }

inline fun View.afterMeasured(crossinline f: View.() -> Unit) {
    viewTreeObserver.addOnGlobalLayoutListener(
        object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (measuredWidth > 0 && measuredHeight > 0) {
                    viewTreeObserver.removeOnGlobalLayoutListener(this)
                    f()
                }
            }
        },
    )
}

fun View.fadeInAnimation(duration: Long = 500) {
    this.alpha = 0f
    this.visibility = View.VISIBLE

    val fadeInAnimation = ObjectAnimator.ofFloat(this, "alpha", 0f, 1f)
    fadeInAnimation.duration = duration
    fadeInAnimation.start()
}

fun View.flash() {
    val originalColor = (background as? ColorDrawable)?.color
    if (this.context.isUsingNightModeResources()) {
        setBackgroundColor(Color.DKGRAY)
    } else {
        setBackgroundColor(Color.LTGRAY)
    }

    postDelayed({
        originalColor?.let { setBackgroundColor(it) } ?: setBackgroundResource(0)
    }, 100)
}
