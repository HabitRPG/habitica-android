package com.habitrpg.android.habitica.ui.views.setup

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.habitrpg.android.habitica.R
import com.habitrpg.common.habitica.extensions.setTintWith

class AvatarCategoryView(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    private val icon: Drawable?
    private val textView: TextView

    init {
        View.inflate(context, R.layout.avatar_category, this)

        textView = findViewById(R.id.text_view)
        val a = context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.AvatarCategoryView,
            0,
            0
        )

        textView.text = a.getText(R.styleable.AvatarCategoryView_categoryTitle)

        icon = a.getDrawable(R.styleable.AvatarCategoryView_iconDrawable)
        if (icon != null) {
            textView.setCompoundDrawablesWithIntrinsicBounds(null, icon, null, null)
        }
        setActive(false)
    }

    fun setActive(active: Boolean) {
        val color: Int = if (active) {
            ContextCompat.getColor(context, R.color.white)
        } else {
            ContextCompat.getColor(context, R.color.white_50_alpha)
        }
        textView.setTextColor(color)
        if (icon != null) {
            icon.setTintWith(color, PorterDuff.Mode.MULTIPLY)
            textView.setCompoundDrawablesWithIntrinsicBounds(null, icon, null, null)
        }
    }
}
