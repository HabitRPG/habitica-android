package com.habitrpg.android.habitica.ui.views.tasks.form

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.extensions.asDrawable
import com.habitrpg.android.habitica.ui.helpers.bindView
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper

class HabitScoringButtonsView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val positiveView: ViewGroup by bindView(R.id.positive_view)
    private val negativeView: ViewGroup by bindView(R.id.negative_view)
    private val positiveImageView: ImageView by bindView(R.id.positive_image_view)
    private val negativeImageView: ImageView by bindView(R.id.negative_image_view)
    private val positiveTextView: TextView by bindView(R.id.positive_text_view)
    private val negativeTextView: TextView by bindView(R.id.negative_text_view)


    var tintColor: Int = ContextCompat.getColor(context, R.color.brand_300)

    var isPositive = true
        set(value) {
            field = value
            positiveImageView.setImageDrawable(HabiticaIconsHelper.imageOfHabitControlPlus(tintColor, value).asDrawable(resources))
            if (value) {
                positiveTextView.setTextColor(tintColor)
                positiveView.contentDescription = toContentDescription(R.string.positive_habit_form, R.string.on)
            } else {
                positiveTextView.setTextColor(ContextCompat.getColor(context, R.color.gray_100))
                positiveView.contentDescription = toContentDescription(R.string.positive_habit_form, R.string.off)
            }
        }

    var isNegative = true
        set(value) {
            field = value
            negativeImageView.setImageDrawable(HabiticaIconsHelper.imageOfHabitControlMinus(tintColor, value).asDrawable(resources))
            if (value) {
                negativeTextView.setTextColor(tintColor)
                negativeView.contentDescription = toContentDescription(R.string.negative_habit_form, R.string.on)
            } else {
                negativeTextView.setTextColor(ContextCompat.getColor(context, R.color.gray_100))
                negativeView.contentDescription = toContentDescription(R.string.negative_habit_form, R.string.off)
            }
        }

    private fun toContentDescription(descriptionStringId: Int, statusStringId: Int): String {
        return context.getString(descriptionStringId) + ", " + context.getString(statusStringId)
    }

    init {
        View.inflate(context, R.layout.task_form_habit_scoring, this)
        gravity = Gravity.CENTER

        positiveView.setOnClickListener {
            isPositive = !isPositive
            sendAccessibilityEvent(AccessibilityEvent.CONTENT_CHANGE_TYPE_CONTENT_DESCRIPTION)
        }
        negativeView.setOnClickListener {
            isNegative = !isNegative
            sendAccessibilityEvent(AccessibilityEvent.CONTENT_CHANGE_TYPE_CONTENT_DESCRIPTION);
        }

        isPositive = true
        isNegative = true
    }
}
