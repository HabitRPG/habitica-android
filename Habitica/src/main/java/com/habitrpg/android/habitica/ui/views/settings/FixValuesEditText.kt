package com.habitrpg.android.habitica.ui.views.settings

import android.content.Context
import android.graphics.Bitmap
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.FixvaluesEdittextBinding
import com.habitrpg.android.habitica.extensions.layoutInflater
import com.habitrpg.android.habitica.extensions.setTintWith
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper

class FixValuesEditText(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {

    private var binding: FixvaluesEdittextBinding
    var text: String
    get() = binding.editText.text.toString()
    set(value) {
        binding.editText.setText(value)
        binding.editText.hint = value
    }

    @ColorRes
    var iconBackgroundColor: Int = 0
    set(value) {
        field = value
        val backgroundDrawable = ContextCompat.getDrawable(context, R.drawable.layout_rounded_bg)
        backgroundDrawable?.setTintWith(field, PorterDuff.Mode.MULTIPLY)
        backgroundDrawable?.alpha = 50
        binding.iconBackgroundView.background = backgroundDrawable
    }

    init {
        val attributes = context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.FixValuesEditText,
                0, 0)

        val view = context.layoutInflater.inflate(R.layout.fixvalues_edittext, this, true)
        binding = FixvaluesEdittextBinding.bind(view)

        binding.editText.hint = attributes.getString(R.styleable.FixValuesEditText_title)
        binding.editTextWrapper.hint = binding.editText.hint
        binding.editTextWrapper.setHintTextAppearance(attributes.getResourceId(R.styleable.FixValuesEditText_hintStyle, R.style.PurpleTextLabel))
        iconBackgroundColor = attributes.getColor(R.styleable.FixValuesEditText_iconBgColor, 0)

        when (attributes.getString(R.styleable.FixValuesEditText_fixIconName)) {
            "health" -> binding.iconView.setImageBitmap(HabiticaIconsHelper.imageOfHeartLightBg())
            "experience" -> binding.iconView.setImageBitmap(HabiticaIconsHelper.imageOfExperience())
            "mana" -> binding.iconView.setImageBitmap(HabiticaIconsHelper.imageOfMagic())
            "gold" -> binding.iconView.setImageBitmap(HabiticaIconsHelper.imageOfGold())
            "level" -> binding.iconView.setImageBitmap(HabiticaIconsHelper.imageOfRogueLightBg())
            "streak" -> binding.iconView.setImageResource(R.drawable.achievement_thermometer)
        }
    }
}
