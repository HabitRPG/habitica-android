package com.habitrpg.android.habitica.ui.views

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.habitrpg.android.habitica.ui.helpers.NavbarUtils

open class PaddedLinearLayout : LinearLayout {
    private var navBarAccountedHeightCalculated = false

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val navbarHeight = NavbarUtils.getNavbarHeight(context)
        val params = layoutParams as? MarginLayoutParams
        params?.setMargins(0, 0, 0, navbarHeight)
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }
}