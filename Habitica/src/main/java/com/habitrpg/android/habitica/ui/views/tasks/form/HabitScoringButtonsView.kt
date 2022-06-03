package com.habitrpg.android.habitica.ui.views.tasks.form

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.Gravity
import android.view.accessibility.AccessibilityEvent
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.TaskFormHabitScoringBinding
import com.habitrpg.android.habitica.extensions.asDrawable
import com.habitrpg.common.habitica.extensions.layoutInflater
import com.habitrpg.common.habitica.views.HabiticaIconsHelper

class HabitScoringButtonsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    private val binding = TaskFormHabitScoringBinding.inflate(context.layoutInflater, this)

    var tintColor: Int = ContextCompat.getColor(context, R.color.brand_300)
    var textTintColor: Int? = null

    override fun setEnabled(isEnabled: Boolean) {
        super.setEnabled(isEnabled)
        binding.positiveView.isEnabled = isEnabled
        binding.negativeView.isEnabled = isEnabled
    }

    var isPositive = true
        set(value) {
            field = value
            binding.positiveImageView.setImageDrawable(HabiticaIconsHelper.imageOfHabitControlPlus(tintColor, value).asDrawable(resources))
            if (value) {
                binding.positiveTextView.setTextColor(textTintColor ?: tintColor)
                binding.positiveView.contentDescription = toContentDescription(R.string.positive_habit_form, R.string.on)
                binding.positiveTextView.typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            } else {
                binding.positiveTextView.setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
                binding.positiveView.contentDescription = toContentDescription(R.string.positive_habit_form, R.string.off)
                binding.positiveTextView.typeface = Typeface.create("sans-serif", Typeface.NORMAL)
            }
        }

    var isNegative = true
        set(value) {
            field = value
            binding.negativeImageView.setImageDrawable(HabiticaIconsHelper.imageOfHabitControlMinus(tintColor, value).asDrawable(resources))
            if (value) {
                binding.negativeTextView.setTextColor(textTintColor ?: tintColor)
                binding.negativeView.contentDescription = toContentDescription(R.string.negative_habit_form, R.string.on)
                binding.negativeTextView.typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            } else {
                binding.negativeTextView.setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
                binding.negativeView.contentDescription = toContentDescription(R.string.negative_habit_form, R.string.off)
                binding.negativeTextView.typeface = Typeface.create("sans-serif", Typeface.NORMAL)
            }
        }

    private fun toContentDescription(descriptionStringId: Int, statusStringId: Int): String {
        return context.getString(descriptionStringId) + ", " + context.getString(statusStringId)
    }

    init {
        gravity = Gravity.CENTER

        binding.positiveView.setOnClickListener {
            isPositive = !isPositive
            sendAccessibilityEvent(AccessibilityEvent.CONTENT_CHANGE_TYPE_CONTENT_DESCRIPTION)
        }
        binding.negativeView.setOnClickListener {
            isNegative = !isNegative
            sendAccessibilityEvent(AccessibilityEvent.CONTENT_CHANGE_TYPE_CONTENT_DESCRIPTION)
        }

        isPositive = true
        isNegative = true
    }
}
