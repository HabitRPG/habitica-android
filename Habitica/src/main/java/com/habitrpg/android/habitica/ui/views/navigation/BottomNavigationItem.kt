package com.habitrpg.android.habitica.ui.views.navigation

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.BottomNavigationItemBinding
import com.habitrpg.android.habitica.extensions.setTintWith
import com.habitrpg.common.habitica.extensions.getThemeColor
import com.habitrpg.common.habitica.extensions.isUsingNightModeResources
import com.habitrpg.common.habitica.extensions.layoutInflater

class BottomNavigationItem @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {
    private var selectedIcon: Drawable? = null
    private var icon: Drawable? = null
    private val binding = BottomNavigationItemBinding.inflate(context.layoutInflater, this)

    private var selectedVisibility = View.VISIBLE
    private var deselectedVisibility = View.VISIBLE

    var isActive = false
        set(value) {
            field = value
            if (isActive) {
                binding.selectedTitleView.visibility = selectedVisibility
                binding.titleView.visibility = View.GONE
                binding.iconView.setImageDrawable(selectedIcon)
                if (context.isUsingNightModeResources()) {
                    binding.iconView.drawable.setTintWith(context.getThemeColor(R.attr.colorPrimaryDistinct), PorterDuff.Mode.SRC_ATOP)
                } else {
                    binding.iconView.drawable.setTintWith(ContextCompat.getColor(context, R.color.white), PorterDuff.Mode.SRC_ATOP)
                }
            } else {
                binding.selectedTitleView.visibility = View.GONE
                binding.iconView.setImageDrawable(icon)
                binding.titleView.visibility = deselectedVisibility
                binding.iconView.drawable.setTintWith(context.getThemeColor(R.attr.textColorPrimaryDark), PorterDuff.Mode.SRC_ATOP)
            }
        }

    var badgeCount: Int = 0
        set(value) {
            field = value
            if (value == 0) {
                binding.badge.visibility = View.INVISIBLE
            } else {
                binding.badge.visibility = View.VISIBLE
                binding.badge.text = value.toString()
            }
        }

    init {
        val attributes = context.theme?.obtainStyledAttributes(
            attrs,
            R.styleable.BottomNavigationItem,
            0,
            0
        )
        if (attributes != null) {
            icon = attributes.getDrawable(R.styleable.BottomNavigationItem_iconDrawable)
            selectedIcon = attributes.getDrawable(R.styleable.BottomNavigationItem_selectedIconDrawable)
            binding.iconView.setImageDrawable(icon)
            binding.titleView.text = attributes.getString(R.styleable.BottomNavigationItem_title)
            binding.selectedTitleView.text = attributes.getString(R.styleable.BottomNavigationItem_title)
        }
    }
}
