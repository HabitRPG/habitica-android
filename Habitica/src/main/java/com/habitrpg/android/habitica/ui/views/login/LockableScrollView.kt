package com.habitrpg.android.habitica.ui.views.login

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.core.widget.NestedScrollView

class LockableScrollView(
    context: Context,
    attrs: AttributeSet,
) : NestedScrollView(context, attrs) {
    var isScrollable = true

    override fun onTouchEvent(ev: MotionEvent): Boolean =
        if (ev.action == MotionEvent.ACTION_DOWN) {
            if (isScrollable) super.onTouchEvent(ev) else false
        } else {
            super.onTouchEvent(ev)
        }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean =
        if (!isScrollable) {
            false
        } else {
            super.onInterceptTouchEvent(ev)
        }
}
