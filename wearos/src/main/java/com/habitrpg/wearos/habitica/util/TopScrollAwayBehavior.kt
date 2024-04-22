package com.habitrpg.wearos.habitica.util

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import kotlin.math.abs
import kotlin.math.min

class TopScrollAwayBehavior<V : View>(context: Context, attrs: AttributeSet) :
    CoordinatorLayout.Behavior<V>(context, attrs) {
    override fun onStartNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: V,
        directTargetChild: View,
        target: View,
        axes: Int,
        type: Int,
    ): Boolean {
        return axes == ViewCompat.SCROLL_AXIS_VERTICAL
    }

    override fun onNestedPreScroll(
        coordinatorLayout: CoordinatorLayout,
        child: V,
        target: View,
        dx: Int,
        dy: Int,
        consumed: IntArray,
        type: Int,
    ) {
        super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type)
        child.translationY = min(0f, -min(child.height.toFloat(), -child.translationY + dy))
    }

    override fun onStopNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: V,
        target: View,
        type: Int,
    ) {
        super.onStopNestedScroll(coordinatorLayout, child, target, type)
        if (child.translationY != 0f && abs(child.translationY) != child.height.toFloat()) {
            if (abs(child.translationY) < (child.height.toFloat() / 2f) && abs(child.translationY) < 40) {
                child.translationY = 0f
            } else {
                child.translationY = if (child.top < child.bottom) -child.height.toFloat() else child.height.toFloat()
            }
        }
    }
}
