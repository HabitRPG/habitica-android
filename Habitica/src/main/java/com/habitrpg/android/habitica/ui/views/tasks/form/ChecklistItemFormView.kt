package com.habitrpg.android.habitica.ui.views.tasks.form

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.TaskFormChecklistItemBinding
import com.habitrpg.android.habitica.extensions.OnChangeTextWatcher
import com.habitrpg.android.habitica.models.tasks.ChecklistItem
import com.habitrpg.common.habitica.extensions.dpToPx
import com.habitrpg.common.habitica.extensions.getThemeColor
import com.habitrpg.common.habitica.extensions.layoutInflater

class ChecklistItemFormView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    val dragGrip: View
        get() = binding.dragGrip
    val binding = TaskFormChecklistItemBinding.inflate(context.layoutInflater, this)

    var item: ChecklistItem = ChecklistItem()
        set(value) {
            field = value
            binding.editText.setText(item.text)
        }

    var tintColor: Int = context.getThemeColor(R.attr.tintedUiSub)
    var textChangedListener: ((String) -> Unit)? = null
    var animDuration = 0L
    var isAddButton: Boolean = true
        set(value) {
            // Button is only clickable when it is *not* an add button (ie when it is a delete button),
            // so make screenreaders skip it when it is an add button.
            binding.button.importantForAccessibility =
                if (value) {
                    View.IMPORTANT_FOR_ACCESSIBILITY_NO
                } else {
                    View.IMPORTANT_FOR_ACCESSIBILITY_YES
                }
            if (field == value) {
                return
            }
            field = value
            binding.editText.hint = context.getString(if (value) R.string.new_checklist_item else R.string.checklist_text)
            val rotate = if (value) {
                contentDescription = context.getString(R.string.new_checklist_item)
                binding.dragGrip.visibility = View.GONE
                RotateAnimation(135f, 0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
            } else {
                binding.dragGrip.visibility = View.VISIBLE
                RotateAnimation(0f, 135f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
            }
            rotate.duration = animDuration
            rotate.interpolator = LinearInterpolator()
            rotate.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationRepeat(animation: Animation?) { /* no-on */ }

                override fun onAnimationEnd(animation: Animation?) {
                    binding.button.rotation = if (value) {
                        0f
                    } else {
                        135f
                    }
                }

                override fun onAnimationStart(animation: Animation?) { /* no-on */ }
            })
            binding.button.startAnimation(rotate)
        }

    init {
        minimumHeight = 38.dpToPx(context)
        background = ContextCompat.getDrawable(context, R.drawable.layout_rounded_bg_task_form)
        background.mutate().setTint(context.getThemeColor(R.attr.colorTintedBackgroundOffset))
        gravity = Gravity.CENTER_VERTICAL

        binding.button.setOnClickListener {
            if (!isAddButton) {
                (parent as? ViewGroup)?.removeView(this)
            }
        }
        // It's ok to make the description always be 'Delete ..' because when this button is
        // a plus button we set it as 'unimportant for accessibility' so it can't be focused.
        binding.button.contentDescription = context.getString(R.string.delete_checklist_entry)
        binding.button.drawable.mutate().setTint(tintColor)
        binding.editText.addTextChangedListener(
            OnChangeTextWatcher { s, _, _, _ ->
                item.text = s.toString()
                contentDescription = s
                textChangedListener?.let { it(s.toString()) }
            }
        )
        binding.editText.labelFor = binding.button.id
    }
}
