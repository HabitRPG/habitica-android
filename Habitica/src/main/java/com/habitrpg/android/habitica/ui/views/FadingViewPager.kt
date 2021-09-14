package com.habitrpg.android.habitica.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.viewpager.widget.ViewPager
import kotlin.math.abs

class FadingViewPager : ViewPager {
    var disableFading: Boolean = false

    constructor(context: Context) : super(context) {
        setTransformer()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        setTransformer()
    }

    private fun setTransformer() {
        this.setPageTransformer(true) { page, position ->
            page.translationX = page.width * -position

            if (position <= -1.0f || position >= 1.0f) {
                page.alpha = 0.0f
                page.visibility = View.INVISIBLE
            } else if (position == 0.0f) {
                page.visibility = View.VISIBLE
                page.alpha = 1.0f
            } else {
                if (disableFading) {
                    return@setPageTransformer
                }
                page.visibility = View.VISIBLE
                // position is between -1.0F & 0.0F OR 0.0F & 1.0F
                page.alpha = 1.0f - abs(position)
            }
        }
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        return false
    }

    override fun onInterceptHoverEvent(event: MotionEvent): Boolean {
        return false
    }
}
