package com.habitrpg.android.habitica.ui.views.tasks.form

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.extensions.OnChangeTextWatcher
import com.habitrpg.android.habitica.extensions.dpToPx
import com.habitrpg.android.habitica.extensions.inflate
import com.habitrpg.android.habitica.models.tasks.ChecklistItem
import com.habitrpg.android.habitica.ui.helpers.bindView


class ChecklistItemFormView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {


    private val button: ImageButton by bindView(R.id.button)
    private val editText: AppCompatEditText by bindView(R.id.edit_text)
    internal val dragGrip: View by bindView(R.id.drag_grip)

    var item: ChecklistItem = ChecklistItem()
    set(value) {
        field = value
        editText.setText(item.text)
    }

    var tintColor: Int = ContextCompat.getColor(context, R.color.brand_300)
    var textChangedListener: ((String) -> Unit)? = null
    var animDuration = 0L
    var isAddButton: Boolean = true
    set(value) {
        if (field == value) {
            return
        }
        field = value
        editText.hint = context.getString(if (value) R.string.new_checklist_item else R.string.checklist_text)
        val rotate = if (value) {
            dragGrip.visibility = View.GONE
            RotateAnimation(135f, 0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        } else {
            dragGrip.visibility = View.VISIBLE
            RotateAnimation(0f, 135f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        }
        rotate.duration = animDuration
        rotate.interpolator = LinearInterpolator()
        rotate.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {}

            override fun onAnimationEnd(animation: Animation?) {
                button.rotation = if (value) {
                    0f
                } else {
                    135f
                }
            }

            override fun onAnimationStart(animation: Animation?) {}
        })
        button.startAnimation(rotate)
    }

    init {
        minimumHeight = 38.dpToPx(context)
        inflate(R.layout.task_form_checklist_item, true)
        background = context.getDrawable(R.drawable.layout_rounded_bg_task_form)
        background.mutate().setTint(ContextCompat.getColor(context, R.color.taskform_gray))
        gravity = Gravity.CENTER_VERTICAL

        button.setOnClickListener {
            if (!isAddButton) {
                (parent as? ViewGroup)?.removeView(this)
            }
        }
        button.drawable.mutate().setTint(tintColor)

        editText.addTextChangedListener(OnChangeTextWatcher { s, _, _, _ ->
            item.text = s.toString()
            textChangedListener?.let { it(s.toString()) }
        })
    }
}