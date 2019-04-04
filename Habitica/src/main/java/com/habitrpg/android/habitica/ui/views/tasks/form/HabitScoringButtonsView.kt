package com.habitrpg.android.habitica.ui.views.tasks.form

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
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

    private val positiveVew: ViewGroup by bindView(R.id.positive_view)
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
            } else {
                positiveTextView.setTextColor(ContextCompat.getColor(context, R.color.gray_100))
            }
        }

    var isNegative = true
        set(value) {
            field = value
            negativeImageView.setImageDrawable(HabiticaIconsHelper.imageOfHabitControlMinus(tintColor, value).asDrawable(resources))
            if (value) {
                negativeTextView.setTextColor(tintColor)
            } else {
                negativeTextView.setTextColor(ContextCompat.getColor(context, R.color.gray_100))
            }
        }

    init {
        View.inflate(context, R.layout.task_form_habit_scoring, this)
        gravity = Gravity.CENTER

        positiveVew.setOnClickListener {
            isPositive = !isPositive
        }
        negativeView.setOnClickListener {
            isNegative = !isNegative
        }

        isPositive = true
        isNegative = true
    }
}
