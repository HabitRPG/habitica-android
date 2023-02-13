package com.habitrpg.android.habitica.ui.views.tasks.form

import android.animation.LayoutTransition
import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.core.view.children
import androidx.core.view.updateMargins
import com.habitrpg.android.habitica.models.tasks.ChecklistItem
import com.habitrpg.android.habitica.ui.views.DragLinearLayout
import com.habitrpg.common.habitica.extensions.dpToPx

class ChecklistContainer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : DragLinearLayout(context, attrs) {
    var checklistItems: List<ChecklistItem>
        get() {
            val list = mutableListOf<ChecklistItem>()
            for (child in children) {
                val view = child as? ChecklistItemFormView ?: continue
                if (view.item.text?.isNotEmpty() == true) {
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
                addChecklistViewAt(childCount - 1, item)
            }
            val animatedTransitions = LayoutTransition()
            layoutTransition = animatedTransitions
        }

    init {
        orientation = VERTICAL

        addChecklistViewAt(0)
    }

    private fun addChecklistViewAt(index: Int, item: ChecklistItem? = null) {
        val view = ChecklistItemFormView(context)
        item?.let {
            view.item = it
            view.isAddButton = false
        }
        view.textChangedListener = {
            if (isLastChild(view)) {
                addChecklistViewAt(-1)
                view.animDuration = 300
                view.isAddButton = false
                setViewDraggable(view, view.dragGrip)
            } else if (shouldBecomeNewAddButton(view)) {
                removeViewAt(childCount - 1)
                view.animDuration = 300
                view.isAddButton = true
                removeViewDraggable(view)
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
            setViewDraggable(view, view.dragGrip)
        }
        val layoutParams = view.layoutParams as? LayoutParams
        layoutParams?.updateMargins(bottom = 8.dpToPx(context))
        view.layoutParams = layoutParams
    }

    private fun shouldBecomeNewAddButton(view: ChecklistItemFormView): Boolean {
        if (childCount > 2 && view.item.text?.isEmpty() != false && children.indexOf(view) == childCount - 2) {
            val lastView = (getChildAt(childCount - 1) as? ChecklistItemFormView)
            if (lastView != null && lastView.item.text?.isEmpty() != false) {
                return true
            }
        }
        return false
    }

    private fun isLastChild(view: View): Boolean {
        return children.lastOrNull() == view
    }
}
