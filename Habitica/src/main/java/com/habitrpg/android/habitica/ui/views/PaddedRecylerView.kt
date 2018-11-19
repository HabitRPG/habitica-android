package com.habitrpg.android.habitica.ui.views

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.habitrpg.android.habitica.ui.helpers.NavbarUtils

open class PaddedRecylerView : RecyclerView {
    private var navBarAccountedHeightCalculated = false

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        if (changed) {
            resizeForDrawingUnderNavbar()
        }
    }

    //https://github.com/roughike/BottomBar/blob/master/bottom-bar/src/main/java/com/roughike/bottombar/BottomBar.java#L834
    private fun resizeForDrawingUnderNavbar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val currentHeight = height

            if (currentHeight != 0 && !navBarAccountedHeightCalculated) {
                navBarAccountedHeightCalculated = true

                val navbarHeight = NavbarUtils.getNavbarHeight(context)
                setPadding(0, 0, 0, navbarHeight)
                (parent as? View)?.invalidate()
            }
        }
    }

}