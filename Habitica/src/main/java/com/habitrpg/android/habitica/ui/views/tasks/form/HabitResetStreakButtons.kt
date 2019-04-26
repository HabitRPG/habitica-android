package com.habitrpg.android.habitica.ui.views.tasks.form

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.extensions.dpToPx
import com.habitrpg.android.habitica.models.tasks.HabitResetOption

class HabitResetStreakButtons @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    var tintColor: Int = ContextCompat.getColor(context, R.color.brand_300)

    var selectedResetOption: HabitResetOption = HabitResetOption.DAILY
    set(value) {
        field = value
        removeAllViews()
        addAllButtons()
    }

    init {
        addAllButtons()
    }

    private fun addAllButtons() {
        val lastResetOption = HabitResetOption.values().last()
        val margin = 16.dpToPx(context)
        val height = 28.dpToPx(context)
        for (resetOption in HabitResetOption.values()) {
            val button = createButton(resetOption)
            val layoutParams = LayoutParams(0, height)
            layoutParams.weight = 1f
            if (resetOption != lastResetOption) {
                layoutParams.marginEnd = margin
            }
            button.textAlignment = View.TEXT_ALIGNMENT_GRAVITY
            button.gravity = Gravity.CENTER
            button.layoutParams = layoutParams
            addView(button)
        }
    }

    private fun createButton(resetOption: HabitResetOption): TextView {
        val button = TextView(context)
        button.text = context.getString(resetOption.nameRes)
        button.background = ContextCompat.getDrawable(context, R.drawable.layout_rounded_bg_white)
        if (selectedResetOption == resetOption) {
            button.background.setTint(tintColor)
            button.setTextColor(ContextCompat.getColor(context, R.color.white))
        } else {
            button.background.setTint(ContextCompat.getColor(context, R.color.taskform_gray))
            button.setTextColor(ContextCompat.getColor(context, R.color.gray_100))
        }
        button.setOnClickListener {
            selectedResetOption = resetOption
        }
        return button
    }
}