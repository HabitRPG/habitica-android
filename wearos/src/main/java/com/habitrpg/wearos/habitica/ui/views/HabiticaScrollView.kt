package com.habitrpg.wearos.habitica.ui.views

import android.content.Context
import android.content.res.Resources
import android.util.AttributeSet
import android.view.View
import androidx.core.view.children
import androidx.core.widget.NestedScrollView
import com.habitrpg.common.habitica.extensions.dpToPx

class HabiticaScrollView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : NestedScrollView(context, attrs) {

    init {
        isVerticalScrollBarEnabled = true
        focusable = View.FOCUSABLE
        isFocusableInTouchMode = true
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        requestFocus()
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        if (changed) {
            val verticalPadding = if (context.resources.configuration.isScreenRound) {
                (0.146467f * Resources.getSystem().displayMetrics.widthPixels).toInt()
            } else {
                0
            }
            val horizontalPadding = 10.dpToPx(context)
            children.firstOrNull()
                ?.setPadding(
                    horizontalPadding,
                    verticalPadding,
                    horizontalPadding,
                    verticalPadding * 2
                )
        }
    }
}
