package com.habitrpg.android.habitica.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.drawerlayout.widget.DrawerLayout

class HabiticaDrawerLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : DrawerLayout(context, attrs, defStyleAttr) {

    var isPersistentMode: Boolean = false

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (isPersistentMode) {
            return false
        }
        return super.onInterceptTouchEvent(ev)
    }
}
