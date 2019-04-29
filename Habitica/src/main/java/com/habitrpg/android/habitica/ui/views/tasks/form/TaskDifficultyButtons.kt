package com.habitrpg.android.habitica.ui.views.tasks.form

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Space
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.extensions.asDrawable
import com.habitrpg.android.habitica.extensions.inflate
import com.habitrpg.android.habitica.models.tasks.TaskDifficulty
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper

class TaskDifficultyButtons @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    var tintColor: Int = ContextCompat.getColor(context, R.color.brand_300)
    var selectedDifficulty: Float = 1f
    set(value) {
        field = value
        removeAllViews()
        addAllButtons()
    }

    init {
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
        }
    }

    private fun createButton(difficulty: TaskDifficulty): View {
        val view = inflate(R.layout.task_form_task_difficulty, false)
        val isActive = selectedDifficulty == difficulty.value
        var difficultyColor = ContextCompat.getColor(context, R.color.white)
        if (isActive) {
            view.findViewById<ImageView>(R.id.image_view).background.mutate().setTint(tintColor)
            view.findViewById<TextView>(R.id.text_view).setTextColor(tintColor)
        } else {
            view.findViewById<ImageView>(R.id.image_view).background.mutate().setTint(ContextCompat.getColor(context, R.color.taskform_gray))
            view.findViewById<TextView>(R.id.text_view).setTextColor(ContextCompat.getColor(context, R.color.gray_100))
            difficultyColor = ContextCompat.getColor(context, R.color.gray_400)
        }
        val drawable = HabiticaIconsHelper.imageOfTaskDifficultyStars(difficultyColor, difficulty.value, true).asDrawable(resources)
        view.findViewById<ImageView>(R.id.image_view).setImageDrawable(drawable)
        view.findViewById<TextView>(R.id.text_view).text = context.getText(difficulty.nameRes)
        view.setOnClickListener {
            selectedDifficulty = difficulty.value
        }
        return view
    }
}