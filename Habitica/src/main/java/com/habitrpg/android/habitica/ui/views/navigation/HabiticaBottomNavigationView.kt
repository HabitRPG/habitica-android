package com.habitrpg.android.habitica.ui.views.navigation

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.BounceInterpolator
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.children
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.MainNavigationViewBinding
import com.habitrpg.android.habitica.extensions.getThemeColor
import com.habitrpg.android.habitica.extensions.layoutInflater
import com.habitrpg.android.habitica.extensions.setTintWith
import com.habitrpg.android.habitica.models.tasks.Task

interface HabiticaBottomNavigationViewListener {
    fun onTabSelected(taskType: String, smooth: Boolean)
    fun onAdd(taskType: String)
}

class HabiticaBottomNavigationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {
    private val binding = MainNavigationViewBinding.inflate(context.layoutInflater, this)

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
    var listener: HabiticaBottomNavigationViewListener? = null
    var activeTaskType: String = Task.TYPE_HABIT
        set(value) {
            val wasChanged = field != value
            field = value
            if (wasChanged) {
                updateItemSelection()
                listener?.onTabSelected(value, true)
            }
        }

    var canAddTasks = true
        set(value) {
            field = value
            if (field) {
                binding.cutoutWrapper.visibility = View.VISIBLE
                binding.cutoutSpace.visibility = View.VISIBLE
                binding.addButtonBackground.visibility = View.VISIBLE
            } else {
                binding.cutoutWrapper.visibility = View.GONE
                binding.cutoutSpace.visibility = View.GONE
                binding.addButtonBackground.visibility = View.GONE
            }
        }

    val barHeight: Int
        get() = binding.itemWrapper.measuredHeight

    init {
        binding.habitsTab.setOnClickListener { activeTaskType = Task.TYPE_HABIT }
        binding.dailiesTab.setOnClickListener { activeTaskType = Task.TYPE_DAILY }
        binding.todosTab.setOnClickListener { activeTaskType = Task.TYPE_TODO }
        binding.rewardsTab.setOnClickListener { activeTaskType = Task.TYPE_REWARD }
        binding.addButton.setOnClickListener {
            if (isShowingSubmenu) {
                hideSubmenu()
            } else {
                listener?.onAdd(activeTaskType)
            }
            animateButtonTap()
        }
        binding.addButton.setOnLongClickListener {
            showSubmenu()
            animateButtonTap()
            true
        }
        binding.addButton.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val animX = ObjectAnimator.ofFloat(binding.addButton, "scaleX", 1f, 1.1f)
                animX.duration = 100
                animX.interpolator = LinearInterpolator()
                animX.start()
                val animY = ObjectAnimator.ofFloat(binding.addButton, "scaleY", 1f, 1.1f)
                animY.duration = 100
                animY.interpolator = LinearInterpolator()
                animY.start()
                val animXBackground = ObjectAnimator.ofFloat(binding.addButtonBackground, "scaleX", 1f, 0.9f)
                animXBackground.duration = 100
                animXBackground.interpolator = LinearInterpolator()
                animXBackground.start()
                val animYBackground = ObjectAnimator.ofFloat(binding.addButtonBackground, "scaleY", 1f, 0.9f)
                animYBackground.duration = 100
                animYBackground.interpolator = LinearInterpolator()
                animYBackground.start()
            }
            false
        }
        binding.submenuWrapper.setOnClickListener { hideSubmenu() }
        updateItemSelection()

        val cutout = ContextCompat.getDrawable(context, R.drawable.bottom_navigation_inset)
        cutout?.setTintWith(context.getThemeColor(R.attr.barColor), PorterDuff.Mode.MULTIPLY)
        binding.cutoutBackground.setImageDrawable(cutout)
        val fabBackground = ContextCompat.getDrawable(context, R.drawable.fab_background)
        fabBackground?.setTintWith(context.getThemeColor(R.attr.colorAccent), PorterDuff.Mode.MULTIPLY)
        binding.addButtonBackground.background = fabBackground
    }

    private fun animateButtonTap() {
        val animX = ObjectAnimator.ofFloat(binding.addButton, "scaleX", 1.3f, 1f)
        animX.duration = 400
        animX.interpolator = BounceInterpolator()
        animX.start()
        val animY = ObjectAnimator.ofFloat(binding.addButton, "scaleY", 1.3f, 1f)
        animY.duration = 400
        animY.interpolator = BounceInterpolator()
        animY.start()
        val animXBackground = ObjectAnimator.ofFloat(binding.addButtonBackground, "scaleX", 0.9f, 1f)
        animXBackground.duration = 600
        animXBackground.interpolator = BounceInterpolator()
        animXBackground.start()
        val animYBackground = ObjectAnimator.ofFloat(binding.addButtonBackground, "scaleY", 0.9f, 1f)
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
        binding.addButton.startAnimation(rotate)

        var pos = 4
        binding.submenuWrapper.removeAllViews()
        for (taskType in listOf(Task.TYPE_HABIT, Task.TYPE_DAILY, Task.TYPE_TODO, Task.TYPE_REWARD)) {
            val view = BottomNavigationSubmenuItem(context)
            when (taskType) {
                Task.TYPE_HABIT -> {
                    view.icon = ContextCompat.getDrawable(context, R.drawable.add_habit)
                    view.title = context.getString(R.string.habit)
                }
                Task.TYPE_DAILY -> {
                    view.icon = ContextCompat.getDrawable(context, R.drawable.add_daily)
                    view.title = context.getString(R.string.daily)
                }
                Task.TYPE_TODO -> {
                    view.icon = ContextCompat.getDrawable(context, R.drawable.add_todo)
                    view.title = context.getString(R.string.todo)
                }
                Task.TYPE_REWARD -> {
                    view.icon = ContextCompat.getDrawable(context, R.drawable.add_rewards)
                    view.title = context.getString(R.string.reward)
                }
            }
            view.onAddListener = {
                listener?.onAdd(taskType)
                hideSubmenu()
            }
            binding.submenuWrapper.addView(view)
            view.alpha = 0f
            view.scaleY = 0.7f
            ViewCompat.animate(view).alpha(1f).setDuration(250.toLong()).startDelay = (100 * pos).toLong()
            ViewCompat.animate(view).scaleY(1f).setDuration(250.toLong()).startDelay = (100 * pos).toLong()
            pos -= 1
        }
        var widestWidth = 0
        for (view in binding.submenuWrapper.children) {
            if (view is BottomNavigationSubmenuItem) {
                val width = view.measuredTitleWidth
                if (widestWidth < width) {
                    widestWidth = width
                }
            }
        }
        for (view in binding.submenuWrapper.children) {
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
        binding.addButton.startAnimation(rotate)

        for (view in binding.submenuWrapper.children) {
            view.alpha = 1f
            view.scaleY = 1f
            ViewCompat.animate(view).alpha(0f).setDuration(200.toLong()).startDelay = (150 * pos).toLong()
            ViewCompat.animate(view).scaleY(0.7f).setDuration(250.toLong()).setStartDelay((100 * pos).toLong()).withEndAction { binding.submenuWrapper.removeView(view) }
            pos += 1
        }
    }

    fun tabWithId(id: Int): BottomNavigationItem? {
        return when (id) {
            R.id.habits_tab -> binding.habitsTab
            R.id.dailies_tab -> binding.dailiesTab
            R.id.todos_tab -> binding.todosTab
            R.id.rewards_tab -> binding.rewardsTab
            else -> null
        }
    }

    private fun updateItemSelection() {
        binding.habitsTab.isActive = activeTaskType == Task.TYPE_HABIT
        binding.dailiesTab.isActive = activeTaskType == Task.TYPE_DAILY
        binding.todosTab.isActive = activeTaskType == Task.TYPE_TODO
        binding.rewardsTab.isActive = activeTaskType == Task.TYPE_REWARD
    }
}
