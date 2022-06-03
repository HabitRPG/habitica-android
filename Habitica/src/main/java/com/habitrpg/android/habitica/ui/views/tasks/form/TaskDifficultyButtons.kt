package com.habitrpg.android.habitica.ui.views.tasks.form

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import android.view.accessibility.AccessibilityEvent
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Space
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.children
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.extensions.asDrawable
import com.habitrpg.android.habitica.extensions.inflate
import com.habitrpg.android.habitica.models.tasks.TaskDifficulty
import com.habitrpg.common.habitica.views.HabiticaIconsHelper

class TaskDifficultyButtons @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    var tintColor: Int = ContextCompat.getColor(context, R.color.brand_300)
    var textTintColor: Int? = null
    var selectedDifficulty: Float = 1f
        set(value) {
            field = value
            removeAllViews()
            addAllButtons()
            selectedButton.sendAccessibilityEvent(AccessibilityEvent.CONTENT_CHANGE_TYPE_CONTENT_DESCRIPTION)
        }
    private lateinit var selectedButton: View

    override fun setEnabled(isEnabled: Boolean) {
        super.setEnabled(isEnabled)
        for (child in this.children) {
            child.isEnabled = isEnabled
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        removeAllViews()
        addAllButtons()
    }

    private fun addAllButtons() {
        val lastDifficulty = TaskDifficulty.values().last()
        for (difficulty in TaskDifficulty.values()) {
            val button = createButton(difficulty)
            addView(button)
            if (difficulty != lastDifficulty) {
                val space = Space(context)
                val layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT)
                layoutParams.weight = 1f
                space.layoutParams = layoutParams
                addView(space)
            }
            if (difficulty.value == selectedDifficulty) {
                selectedButton = button
            }
        }
    }

    private fun createButton(difficulty: TaskDifficulty): View {
        val view = inflate(R.layout.task_form_task_difficulty, false)
        val isActive = selectedDifficulty == difficulty.value
        var difficultyColor = ContextCompat.getColor(context, R.color.white)
        if (isActive) {
            view.findViewById<ImageView>(R.id.image_view).background.mutate().setTint(tintColor)
            view.findViewById<TextView>(R.id.text_view).setTextColor(textTintColor ?: tintColor)
            view.findViewById<TextView>(R.id.text_view).typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        } else {
            view.findViewById<ImageView>(R.id.image_view).background.mutate().setTint(ContextCompat.getColor(context, R.color.taskform_gray))
            view.findViewById<TextView>(R.id.text_view).setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
            difficultyColor = ContextCompat.getColor(context, R.color.disabled_background)
            view.findViewById<TextView>(R.id.text_view).typeface = Typeface.create("sans-serif", Typeface.NORMAL)
        }
        val drawable = HabiticaIconsHelper.imageOfTaskDifficultyStars(difficultyColor, difficulty.value, true).asDrawable(resources)
        view.findViewById<ImageView>(R.id.image_view).setImageDrawable(drawable)

        val buttonText = context.getText(difficulty.nameRes)
        view.findViewById<TextView>(R.id.text_view).text = buttonText
        view.contentDescription = toContentDescription(buttonText, isActive)

        view.setOnClickListener {
            selectedDifficulty = difficulty.value
        }
        return view
    }

    private fun toContentDescription(buttonText: CharSequence, isActive: Boolean): String {
        val statusString = if (isActive) {
            context.getString(R.string.selected)
        } else context.getString(R.string.not_selected)
        return "$buttonText, $statusString"
    }
}
