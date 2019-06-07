package com.habitrpg.android.habitica.ui.views.navigation

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.extensions.inflate
import com.habitrpg.android.habitica.ui.helpers.bindView

class BottomNavigationSubmenuItem @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    var onAddListener: (() -> Unit)? = null

    val measuredTitleWidth: Int
    get() {
        titleView.measure(width, height)
        return titleView.measuredWidth
    }
    private val iconView: ImageView by bindView(R.id.icon_view)
    private val titleView: TextView by bindView(R.id.title_view)

    var icon: Drawable? = null
    set(value) {
        field = value
        iconView.setImageDrawable(value)
    }
    var title: String? = null
    set(value) {
        field = value
        titleView.text = title
    }

    init {
        inflate(R.layout.bottom_navigation_submenu, true)
        iconView.setOnClickListener { onAddListener?.invoke() }
        titleView.setOnClickListener { onAddListener?.invoke() }
    }

    fun setTitleWidth(width: Int) {
        val layoutParams = titleView.layoutParams as? LayoutParams
        layoutParams?.width = width
        titleView.layoutParams = layoutParams
    }

}