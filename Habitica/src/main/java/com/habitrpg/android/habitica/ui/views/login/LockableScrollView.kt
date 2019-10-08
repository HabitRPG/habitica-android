package com.habitrpg.android.habitica.ui.views.login

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.ScrollView

class LockableScrollView(context: Context, attrs: AttributeSet) : ScrollView(context, attrs) {

    private var isScrollable = true

    fun setScrollingEnabled(enabled: Boolean) {
        isScrollable = enabled
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        return if (ev.action == MotionEvent.ACTION_DOWN) {
            if (isScrollable) super.onTouchEvent(ev) else isScrollable
        } else {
            super.onTouchEvent(ev)
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return if (!isScrollable)
            false
        else
            super.onInterceptTouchEvent(ev)
    }

}
