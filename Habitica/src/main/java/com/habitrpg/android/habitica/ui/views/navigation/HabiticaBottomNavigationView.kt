package com.habitrpg.android.habitica.ui.views.navigation

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageButton
import android.widget.RelativeLayout
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.extensions.inflate
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.ui.helpers.bindView

class HabiticaBottomNavigationView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    var selectedPosition: Int
    get() {
        return when (activeTaskType) {
            Task.TYPE_DAILY -> 1
            Task.TYPE_REWARD -> 2
            Task.TYPE_TODO -> 3
            else -> 0
        }
    }
    set(value) {
        activeTaskType = when (value) {
            1 -> Task.TYPE_DAILY
            2 -> Task.TYPE_TODO
            3 -> Task.TYPE_REWARD
            else -> Task.TYPE_HABIT
        }
    }
    var onTabSelectedListener: ((String) -> Unit)? = null
    var onAddListener: ((String) -> Unit)? = null
    var activeTaskType: String = Task.TYPE_HABIT
    set(value) {
        field = value
        updateItemSelection()
        onTabSelectedListener?.invoke(value)
    }

    private val habitsTab: BottomNavigationItem by bindView(R.id.tab_habits)
    private val dailiesTab: BottomNavigationItem by bindView(R.id.tab_dailies)
    private val todosTab: BottomNavigationItem by bindView(R.id.tab_todos)
    private val rewardsTab: BottomNavigationItem by bindView(R.id.tab_rewards)
    private val addButton: ImageButton by bindView(R.id.add)

    init {
        inflate(R.layout.main_navigation_view, true)
        habitsTab.setOnClickListener { activeTaskType = Task.TYPE_HABIT }
        dailiesTab.setOnClickListener { activeTaskType = Task.TYPE_DAILY }
        todosTab.setOnClickListener { activeTaskType = Task.TYPE_TODO }
        rewardsTab.setOnClickListener { activeTaskType = Task.TYPE_REWARD }
        addButton.setOnClickListener {
            onAddListener?.invoke(activeTaskType)
        }
        updateItemSelection()
    }

    private fun updateItemSelection() {
        habitsTab.isActive = activeTaskType == Task.TYPE_HABIT
        dailiesTab.isActive = activeTaskType == Task.TYPE_DAILY
        todosTab.isActive = activeTaskType == Task.TYPE_TODO
        rewardsTab.isActive = activeTaskType == Task.TYPE_REWARD
    }
}