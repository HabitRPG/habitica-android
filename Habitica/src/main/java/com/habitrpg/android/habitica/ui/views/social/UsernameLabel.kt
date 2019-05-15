package com.habitrpg.android.habitica.ui.views.social

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.models.members.PlayerTier
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper

class UsernameLabel(context: Context?, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    private val textView = TextView(context)
    private val tierIconView = ImageView(context)

    var username: String? = ""
    set(value) {
        field = value
        textView.text = value
    }

    var tier: Int = 0
    set(value) {
        field = value
        textView.setTextColor(PlayerTier.getColorForTier(context, value))
        when (value) {
            0 -> tierIconView.visibility = View.GONE
            else -> {
                tierIconView.visibility = View.VISIBLE
                tierIconView.setImageBitmap(HabiticaIconsHelper.imageOfContributorBadge(value.toFloat(), false))
            }
        }
    }

    init {
        val textViewParams = LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT)
        textViewParams.gravity = Gravity.CENTER_VERTICAL
        textViewParams.weight = 1.0f
        addView(textView, textViewParams)
        val padding = context?.resources?.getDimension(R.dimen.spacing_small)?.toInt() ?: 0
        textView.setPadding(0, 0, padding, 0)
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            textView.setTextAppearance(R.style.Body1)
        } else {
            textView.setTextAppearance(context, R.style.Body1)
        }
        val iconViewParams = LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT)
        iconViewParams.gravity = Gravity.CENTER_VERTICAL
        addView(tierIconView, iconViewParams)
    }
}