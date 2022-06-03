package com.habitrpg.android.habitica.ui.views.navigation

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.RelativeLayout
import com.habitrpg.android.habitica.databinding.BottomNavigationSubmenuBinding
import com.habitrpg.common.habitica.extensions.layoutInflater

class BottomNavigationSubmenuItem @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {
    private val binding = BottomNavigationSubmenuBinding.inflate(context.layoutInflater, this)

    var onAddListener: (() -> Unit)? = null

    val measuredTitleWidth: Int
        get() {
            binding.titleView.measure(width, height)
            return binding.titleView.measuredWidth
        }

    var icon: Drawable? = null
        set(value) {
            field = value
            binding.iconView.setImageDrawable(value)
        }
    var title: String? = null
        set(value) {
            field = value
            binding.titleView.text = title
        }

    init {
        binding.iconView.setOnClickListener { onAddListener?.invoke() }
        binding.titleView.setOnClickListener { onAddListener?.invoke() }
    }

    fun setTitleWidth(width: Int) {
        val layoutParams = binding.titleView.layoutParams as? LayoutParams
        layoutParams?.width = width
        binding.titleView.layoutParams = layoutParams
    }
}
