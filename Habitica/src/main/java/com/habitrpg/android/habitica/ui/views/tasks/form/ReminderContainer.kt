package com.habitrpg.android.habitica.ui.views.tasks.form

import android.animation.LayoutTransition
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import androidx.core.view.children
import androidx.core.view.updateMargins
import com.habitrpg.android.habitica.extensions.dpToPx
import com.habitrpg.android.habitica.models.tasks.RemindersItem
import com.habitrpg.android.habitica.models.tasks.Task
import io.realm.RealmList

class ReminderContainer @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    var taskType = Task.TYPE_DAILY
    set(value) {
        field = value
        for (view in children) {
            if (view is ReminderItemFormView) {
                view.taskType = taskType
            }
        }
    }
    var reminders: RealmList<RemindersItem>
        get() {
            val list = RealmList<RemindersItem>()
            for (child in children) {
                val view = child as? ReminderItemFormView ?: continue
                if (view.item.time != null) {
                    list.add(view.item)
                }
            }
            return list
        }
        set(value) {
            val unAnimatedTransitions = LayoutTransition()
            unAnimatedTransitions.disableTransitionType(LayoutTransition.APPEARING)
            unAnimatedTransitions.disableTransitionType(LayoutTransition.CHANGING)
            unAnimatedTransitions.disableTransitionType(LayoutTransition.DISAPPEARING)
            layoutTransition = unAnimatedTransitions
            if (childCount > 1) {
                for (child in children.take(childCount - 1)) {
                    removeView(child)
                }
            }
            for (item in value) {
                addReminderViewAt(childCount-1, item)
            }
            val animatedTransitions = LayoutTransition()
            layoutTransition = animatedTransitions
        }

    init {
        orientation = VERTICAL

        addReminderViewAt(0)
    }

    private fun addReminderViewAt(index: Int, item: RemindersItem? = null) {
        val view = ReminderItemFormView(context)
        view.taskType = taskType
        item?.let {
            view.item = it
            view.isAddButton = false
        }
        view.valueChangedListener = {
            if (isLastChild(view)) {
                addReminderViewAt(-1)
                view.animDuration = 300
                view.isAddButton = false
            }
        }
        val indexToUse = if (index < 0) {
            childCount - index
        } else {
            index
        }
        if (childCount <= indexToUse) {
            addView(view)
            view.isAddButton = true
        } else {
            addView(view, indexToUse)
        }
        val layoutParams = view.layoutParams as? LayoutParams
        layoutParams?.updateMargins(bottom = 8.dpToPx(context))
        view.layoutParams = layoutParams
    }

    private fun isLastChild(view: View): Boolean {
        return children.lastOrNull() == view
    }
}