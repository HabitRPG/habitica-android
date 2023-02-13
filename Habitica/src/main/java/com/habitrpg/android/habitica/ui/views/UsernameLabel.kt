package com.habitrpg.android.habitica.ui.views

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.habitrpg.common.habitica.R
import com.habitrpg.common.habitica.models.PlayerTier

class UsernameLabel @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    private val textView = TextView(context)
    private val tierIconView = ImageView(context)

    var username: String? = ""
        set(value) {
            field = value
            textView.text = value
        }

    var isNPC: Boolean = false
        set(value) {
            field = value
            tier = tier
        }

    var tier: Int = 0
        set(value) {
            field = value
            if (isNPC) {
                textView.setTextColor(ContextCompat.getColor(context, R.color.contributor_npc))
            } else {
                textView.setTextColor(PlayerTier.getColorForTier(context, value))
            }
            if (value == 0) {
                tierIconView.visibility = View.GONE
            } else {
                tierIconView.visibility = View.VISIBLE
                tierIconView.setImageBitmap(com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper.imageOfContributorBadge(value.toFloat(), isNPC))
            }
        }

    init {
        val textViewParams = LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        textViewParams.gravity = Gravity.CENTER_VERTICAL
        textViewParams.weight = 1.0f
        addView(textView, textViewParams)
        val padding = context.resources.getDimension(R.dimen.spacing_small).toInt()
        textView.setPadding(0, 0, padding, 0)
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
        val iconViewParams = LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        iconViewParams.gravity = Gravity.CENTER_VERTICAL
        addView(tierIconView, iconViewParams)
    }
}
