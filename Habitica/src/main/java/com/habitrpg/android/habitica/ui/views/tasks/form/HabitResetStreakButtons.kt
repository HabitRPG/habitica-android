package com.habitrpg.android.habitica.ui.views.tasks.form

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.accessibility.AccessibilityEvent
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.habitrpg.android.habitica.R
import com.habitrpg.common.habitica.extensions.dpToPx
import com.habitrpg.common.habitica.extensions.nameRes
import com.habitrpg.shared.habitica.models.tasks.HabitResetOption

class HabitResetStreakButtons @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    var tintColor: Int = ContextCompat.getColor(context, R.color.brand_300)

    var selectedResetOption: HabitResetOption = HabitResetOption.DAILY
        set(value) {
            field = value
            removeAllViews()
            addAllButtons()
            selectedButton.sendAccessibilityEvent(
                AccessibilityEvent.CONTENT_CHANGE_TYPE_CONTENT_DESCRIPTION
            )
        }

    private lateinit var selectedButton: TextView

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
            if (resetOption == selectedResetOption) {
                selectedButton = button
            }
        }
    }

    private fun createButton(resetOption: HabitResetOption): TextView {
        val isActive = selectedResetOption == resetOption

        val button = TextView(context)
        val buttonText = context.getString(resetOption.nameRes)
        button.text = buttonText
        button.contentDescription = toContentDescription(buttonText, isActive)
        button.background = ContextCompat.getDrawable(context, R.drawable.layout_rounded_bg_content)

        if (isActive) {
            button.background.setTint(tintColor)
            button.setTextColor(ContextCompat.getColor(context, R.color.white))
            button.typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        } else {
            button.background.setTint(ContextCompat.getColor(context, R.color.taskform_gray))
            button.setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
            button.typeface = Typeface.create("sans-serif", Typeface.NORMAL)
        }
        button.setOnClickListener {
            selectedResetOption = resetOption
        }
        return button
    }

    private fun toContentDescription(buttonText: CharSequence, isActive: Boolean): String {
        val statusString = if (isActive) {
            context.getString(R.string.selected)
        } else context.getString(R.string.not_selected)
        return "$buttonText, $statusString"
    }
}
