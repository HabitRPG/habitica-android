package com.habitrpg.android.habitica.ui.views.settings

import android.content.Context
import android.graphics.Bitmap
import android.graphics.PorterDuff
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper
import kotlinx.android.synthetic.main.fixvalues_edittext.view.*

class FixValuesEditText(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {

    var text: String
    get() = editText?.text.toString()
    set(value) {
        editText?.setText(value)
    }

    @ColorRes
    var iconBackgroundColor: Int = 0
    set(value) {
        field = value
        val backgroundDrawable = ContextCompat.getDrawable(context, R.drawable.layout_rounded_bg)
        backgroundDrawable?.setColorFilter(field, PorterDuff.Mode.MULTIPLY)
        backgroundDrawable?.alpha = 50
        iconBackgroundView.background = backgroundDrawable
    }

    init {
        View.inflate(context, R.layout.fixvalues_edittext, this)

        val attributes = context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.FixValuesEditText,
                0, 0)

        editText.hint = attributes.getString(R.styleable.FixValuesEditText_title)
        editTextWrapper.hint = editText.hint
        editTextWrapper.setHintTextAppearance(attributes.getResourceId(R.styleable.FixValuesEditText_hintStyle, R.style.PurpleTextLabel))
        iconBackgroundColor = attributes.getColor(R.styleable.FixValuesEditText_iconBgColor, 0)

        val iconName = attributes.getString(R.styleable.FixValuesEditText_fixIconName)
        when (iconName) {
            "health" -> iconView.setImageBitmap(HabiticaIconsHelper.imageOfHeartLightBg())
            "experience" -> iconView.setImageBitmap(HabiticaIconsHelper.imageOfExperience())
            "mana" -> iconView.setImageBitmap(HabiticaIconsHelper.imageOfMagic())
            "gold" -> iconView.setImageBitmap(HabiticaIconsHelper.imageOfGold())
            "level" -> iconView.setImageBitmap(HabiticaIconsHelper.imageOfRogueLightBg())
            "streak" -> iconView.setImageResource(R.drawable.achievement_thermometer)
        }
    }

    fun setIconBitmap(icon: Bitmap) {
        iconView.setImageBitmap(icon)
    }
}
