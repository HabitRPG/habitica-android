package com.habitrpg.wearos.habitica.ui.views

import android.content.Context
import android.content.res.Resources
import android.util.AttributeSet
import androidx.core.view.children
import androidx.core.view.setPadding
import androidx.core.widget.NestedScrollView

class HabiticaScrollView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : NestedScrollView(context, attrs) {

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        if (changed) {
            if (context.resources.configuration.isScreenRound) {
                children.firstOrNull()
                    ?.setPadding((0.146467f * Resources.getSystem().displayMetrics.widthPixels).toInt())
            }
        }
    }
}