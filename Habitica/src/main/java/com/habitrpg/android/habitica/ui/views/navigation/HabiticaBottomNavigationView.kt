package com.habitrpg.android.habitica.ui.views.navigation

import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.BounceInterpolator
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.core.view.ViewCompat
import androidx.core.view.children
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.extensions.inflate
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.ui.helpers.bindView


class HabiticaBottomNavigationView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    var flipAddBehaviour = true
    private var isShowingSubmenu: Boolean = false
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
    var onAddListener:  ((String) -> Unit)? = null
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
    private val addButtonBackground: ViewGroup by bindView(R.id.add_wrapper)
    private val submenuWrapper: LinearLayout by bindView(R.id.submenu_wrapper)

    init {
        inflate(R.layout.main_navigation_view, true)
        habitsTab.setOnClickListener { activeTaskType = Task.TYPE_HABIT }
        dailiesTab.setOnClickListener { activeTaskType = Task.TYPE_DAILY }
        todosTab.setOnClickListener { activeTaskType = Task.TYPE_TODO }
        rewardsTab.setOnClickListener { activeTaskType = Task.TYPE_REWARD }
        addButton.setOnClickListener {
            if (flipAddBehaviour) {
                if (isShowingSubmenu) {
                    hideSubmenu()
                } else {
                    onAddListener?.invoke(activeTaskType)
                }
            } else {
                showSubmenu()
            }
            animateButtonTap()
        }
        addButton.setOnLongClickListener {
            if (flipAddBehaviour) {
                showSubmenu()
            } else {
                onAddListener?.invoke(activeTaskType)
            }
            animateButtonTap()
            true
        }
        addButton.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val animX = ObjectAnimator.ofFloat(addButton, "scaleX", 1f, 1.1f)
                animX.duration = 100
                animX.interpolator = LinearInterpolator()
                animX.start()
                val animY = ObjectAnimator.ofFloat(addButton, "scaleY", 1f, 1.1f)
                animY.duration = 100
                animY.interpolator = LinearInterpolator()
                animY.start()
                val animXBackground = ObjectAnimator.ofFloat(addButtonBackground, "scaleX", 1f, 0.9f)
                animXBackground.duration = 100
                animXBackground.interpolator = LinearInterpolator()
                animXBackground.start()
                val animYBackground = ObjectAnimator.ofFloat(addButtonBackground, "scaleY", 1f, 0.9f)
                animYBackground.duration = 100
                animYBackground.interpolator = LinearInterpolator()
                animYBackground.start()
            }
            false
        }
        submenuWrapper.setOnClickListener { hideSubmenu() }
        updateItemSelection()
    }

    private fun animateButtonTap() {
        val animX = ObjectAnimator.ofFloat(addButton, "scaleX", 1.3f, 1f)
        animX.duration = 400
        animX.interpolator = BounceInterpolator()
        animX.start()
        val animY = ObjectAnimator.ofFloat(addButton, "scaleY", 1.3f, 1f)
        animY.duration = 400
        animY.interpolator = BounceInterpolator()
        animY.start()
        val animXBackground = ObjectAnimator.ofFloat(addButtonBackground, "scaleX", 0.9f, 1f)
        animXBackground.duration = 600
        animXBackground.interpolator = BounceInterpolator()
        animXBackground.start()
        val animYBackground = ObjectAnimator.ofFloat(addButtonBackground, "scaleY", 0.9f, 1f)
        animYBackground.duration = 600
        animYBackground.interpolator = BounceInterpolator()
        animYBackground.start()
    }

    private fun showSubmenu() {
        if (isShowingSubmenu) return
        isShowingSubmenu = true

        val rotate = RotateAnimation(0f, 135f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        rotate.duration = 250
        rotate.interpolator = LinearInterpolator()
        rotate.fillAfter = true
        addButton.startAnimation(rotate)

        var pos = 4
        submenuWrapper.removeAllViews()
        for (taskType in listOf(Task.TYPE_HABIT, Task.TYPE_DAILY, Task.TYPE_TODO, Task.TYPE_REWARD)) {
            val view = BottomNavigationSubmenuItem(context)
            when (taskType) {
                Task.TYPE_HABIT -> {
                    view.icon = context.getDrawable(R.drawable.add_habit)
                    view.title = context.getString(R.string.habit)
                }
                Task.TYPE_DAILY -> {
                    view.icon = context.getDrawable(R.drawable.add_daily)
                    view.title = context.getString(R.string.daily)
                }
                Task.TYPE_TODO -> {
                    view.icon = context.getDrawable(R.drawable.add_todo)
                    view.title = context.getString(R.string.todo)
                }
                Task.TYPE_REWARD -> {
                    view.icon = context.getDrawable(R.drawable.add_rewards)
                    view.title = context.getString(R.string.reward)
                }
            }
            view.onAddListener = {
                onAddListener?.invoke(taskType)
                hideSubmenu()
            }
            submenuWrapper.addView(view)
            view.alpha = 0f
            view.scaleY = 0.7f
            ViewCompat.animate(view).alpha(1f).setDuration(250.toLong()).startDelay = (100 * pos).toLong()
            ViewCompat.animate(view).scaleY(1f).setDuration(250.toLong()).startDelay = (100 * pos).toLong()
            pos -= 1
        }
        var widestWidth = 0
        for (view in submenuWrapper.children) {
            if (view is BottomNavigationSubmenuItem) {
                val width = view.measuredTitleWidth
                if (widestWidth < width) {
                    widestWidth = width
                }
            }
        }
        for (view in submenuWrapper.children) {
            if (view is BottomNavigationSubmenuItem) {
                view.setTitleWidth(widestWidth)
            }
        }
    }

    private fun hideSubmenu() {
        isShowingSubmenu = false
        var pos = 0

        val rotate = RotateAnimation(135f, 0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        rotate.duration = 250
        rotate.interpolator = LinearInterpolator()
        rotate.fillAfter = true
        addButton.startAnimation(rotate)

        for (view in submenuWrapper.children) {
            view.alpha = 1f
            view.scaleY = 1f
            ViewCompat.animate(view).alpha(0f).setDuration(200.toLong()).startDelay = (150 * pos).toLong()
            ViewCompat.animate(view).scaleY(0.7f).setDuration(250.toLong()).setStartDelay((100 * pos).toLong()).withEndAction { submenuWrapper.removeView(view) }
            pos += 1
        }
    }

    fun tabWithId(id: Int): BottomNavigationItem? {
        return when(id) {
            R.id.tab_habits -> habitsTab
            R.id.tab_dailies -> dailiesTab
            R.id.tab_todos -> rewardsTab
            R.id.tab_rewards -> todosTab
            else -> null
        }
    }

    private fun updateItemSelection() {
        habitsTab.isActive = activeTaskType == Task.TYPE_HABIT
        dailiesTab.isActive = activeTaskType == Task.TYPE_DAILY
        todosTab.isActive = activeTaskType == Task.TYPE_TODO
        rewardsTab.isActive = activeTaskType == Task.TYPE_REWARD
    }
}