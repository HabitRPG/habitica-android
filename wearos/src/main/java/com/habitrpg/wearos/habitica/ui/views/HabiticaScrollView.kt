package com.habitrpg.wearos.habitica.ui.views

import android.content.Context
import android.content.res.Resources
import android.util.AttributeSet
import androidx.core.view.children
import androidx.core.widget.NestedScrollView

class HabiticaScrollView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : NestedScrollView(context, attrs) {

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        if (changed) {
            if (context.resources.configuration.isScreenRound) {
                val verticalPadding =
                    (0.146467f * Resources.getSystem().displayMetrics.widthPixels).toInt()
                val horizontalPadding =
                    (0.1f * Resources.getSystem().displayMetrics.widthPixels).toInt()
                children.firstOrNull()
                    ?.setPadding(
                        horizontalPadding,
                        verticalPadding,
                        horizontalPadding,
                        verticalPadding*2
                    )
            }
        }
    }
}