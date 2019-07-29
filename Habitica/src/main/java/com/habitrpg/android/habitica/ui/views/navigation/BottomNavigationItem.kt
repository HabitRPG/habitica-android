package com.habitrpg.android.habitica.ui.views.navigation

import android.content.Context
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.extensions.inflate
import com.habitrpg.android.habitica.ui.helpers.bindView

class BottomNavigationItem @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    private val iconView: ImageView by bindView(R.id.icon_view)
    private val selectedTitleView: TextView by bindView(R.id.selected_title_view)
    private val titleView: TextView by bindView(R.id.title_view)
    private val badge: TextView by bindView(R.id.badge)

    var isActive = false
    set(value) {
        field = value
        if (isActive) {
            selectedTitleView.visibility = View.VISIBLE
            titleView.visibility = View.GONE
            iconView.drawable.setColorFilter(ContextCompat.getColor(context, R.color.white), PorterDuff.Mode.MULTIPLY )
        } else {
            selectedTitleView.visibility = View.GONE
            titleView.visibility = View.VISIBLE
            iconView.drawable.setColorFilter(ContextCompat.getColor(context, R.color.brand_500), PorterDuff.Mode.MULTIPLY )
        }
    }

    var badgeCount: Int = 0
    set(value) {
        field = value
        if (value == 0) {
            badge.visibility = View.INVISIBLE
        } else {
            badge.visibility = View.VISIBLE
            badge.text = value.toString()
        }
    }

    init {
        inflate(R.layout.bottom_navigation_item, true)

        val attributes = context.theme?.obtainStyledAttributes(
                attrs,
                R.styleable.BottomNavigationItem,
                0, 0)
        if (attributes != null) {
            iconView.setImageDrawable(attributes.getDrawable(R.styleable.BottomNavigationItem_iconDrawable))
            titleView.text = attributes.getString(R.styleable.BottomNavigationItem_title)
            selectedTitleView.text = attributes.getString(R.styleable.BottomNavigationItem_title)
        }
    }

}