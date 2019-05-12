package com.habitrpg.android.habitica.ui.fragments.tasks

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.FragmentPagerAdapter
import android.view.*
import com.github.clans.fab.FloatingActionButton
import com.github.clans.fab.FloatingActionMenu
import com.habitrpg.android.habitica.HabiticaBaseApplication
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.AppComponent
import com.habitrpg.android.habitica.data.TagRepository
import com.habitrpg.android.habitica.events.TaskTappedEvent
import com.habitrpg.android.habitica.extensions.notNull
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.helpers.TaskFilterHelper
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.ui.activities.TaskFormActivity
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment
import com.habitrpg.android.habitica.ui.views.tasks.TaskFilterDialog
import io.reactivex.functions.Consumer
import org.greenrobot.eventbus.Subscribe
import java.util.*
import javax.inject.Inject

class TasksFragment : BaseMainFragment() {

    var viewPager: androidx.viewpager.widget.ViewPager? = null
    @Inject
    lateinit var taskFilterHelper: TaskFilterHelper
    @Inject
    lateinit var tagRepository: TagRepository

    private var refreshItem: MenuItem? = null
    private var floatingMenu: FloatingActionMenu? = null
    internal var viewFragmentsDictionary: MutableMap<Int, TaskRecyclerViewFragment>? = WeakHashMap()

    private var displayingTaskForm: Boolean = false
    private var filterMenuItem: MenuItem? = null

    private val activeFragment: TaskRecyclerViewFragment?
        get() = viewFragmentsDictionary?.get(viewPager?.currentItem)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        this.usesTabLayout = false
        this.usesBottomNavigation = true
        this.displayingTaskForm = false
        super.onCreateView(inflater, container, savedInstanceState)
        val v = inflater.inflate(R.layout.fragment_viewpager, container, false)


        viewPager = v.findViewById(R.id.viewPager)
        val view = inflater.inflate(R.layout.floating_menu_tasks, floatingMenuWrapper, true)
        floatingMenu = if (FloatingActionMenu::class.java == view.javaClass) {
            view as? FloatingActionMenu
        } else {
            val frame = view as? ViewGroup
            frame?.findViewById(R.id.fab_menu)
        }
        val habitFab = floatingMenu?.findViewById<FloatingActionButton>(R.id.fab_new_habit)
        habitFab?.setOnClickListener { openNewTaskActivity(Task.TYPE_HABIT) }
        val dailyFab = floatingMenu?.findViewById<FloatingActionButton>(R.id.fab_new_daily)
        dailyFab?.setOnClickListener { openNewTaskActivity(Task.TYPE_DAILY) }
        val todoFab = floatingMenu?.findViewById<FloatingActionButton>(R.id.fab_new_todo)
        todoFab?.setOnClickListener { openNewTaskActivity(Task.TYPE_TODO) }
        val rewardFab = floatingMenu?.findViewById<FloatingActionButton>(R.id.fab_new_reward)
        rewardFab?.setOnClickListener { openNewTaskActivity(Task.TYPE_REWARD) }
        floatingMenu?.setOnMenuButtonLongClickListener { this.onFloatingMenuLongClicked() }

        loadTaskLists()

        return v
    }

    override fun onResume() {
        super.onResume()

        bottomNavigation?.setBadgesHideWhenActive(true)
        bottomNavigation?.setOnTabSelectListener { tabId ->
            when (tabId) {
                R.id.tab_habits -> viewPager?.currentItem = 0
                R.id.tab_dailies -> viewPager?.currentItem = 1
                R.id.tab_todos -> viewPager?.currentItem = 2
                R.id.tab_rewards -> viewPager?.currentItem = 3
            }
            updateBottomBarBadges()
        }
    }

    override fun onDestroy() {
        tagRepository.close()
        bottomNavigation?.removeOnTabSelectListener()
        super.onDestroy()
    }

    private fun onFloatingMenuLongClicked(): Boolean {
        val currentFragment = activeFragment
        if (currentFragment != null) {
            val className = currentFragment.className
            openNewTaskActivity(className)
        }
        return true
    }

    override fun injectFragment(component: AppComponent) {
        component.inject(this)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main_activity, menu)

        filterMenuItem = menu.findItem(R.id.action_search)
        updateFilterIcon()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        when (id) {
            R.id.action_search -> {
                showFilterDialog()
                return true
            }
            R.id.action_reload -> {
                refreshItem = item
                refresh()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun showFilterDialog() {
        context.notNull {
            val dialog = TaskFilterDialog(it, HabiticaBaseApplication.component)
            if (user != null) {
                dialog.setTags(user?.tags?.createSnapshot() ?: emptyList())
            }
            dialog.setActiveTags(taskFilterHelper.tags)
            if (activeFragment != null) {
                val taskType = activeFragment?.classType
                if (taskType != null) {
                    dialog.setTaskType(taskType, taskFilterHelper.getActiveFilter(taskType))
                }
            }
            dialog.setListener(object : TaskFilterDialog.OnFilterCompletedListener {

                override fun onFilterCompleted(activeTaskFilter: String?, activeTags: MutableList<String>) {
                    if (viewFragmentsDictionary == null) {
                        return
                    }
                    val activePos = viewPager?.currentItem ?: 0
                    viewFragmentsDictionary?.get(activePos - 1)?.recyclerAdapter?.filter()
                    viewFragmentsDictionary?.get(activePos + 1)?.recyclerAdapter?.filter()
                    taskFilterHelper.tags = activeTags
                    if (activeTaskFilter != null) {
                        activeFragment?.setActiveFilter(activeTaskFilter)
                    }
                    viewFragmentsDictionary?.values?.forEach { it.recyclerAdapter?.filter() }
                    updateFilterIcon()
                }
            })
            dialog.show()
        }
    }

    private fun refresh() {
        activeFragment?.onRefresh()
    }

    private fun loadTaskLists() {
        val fragmentManager = childFragmentManager

        viewPager?.adapter = object : FragmentPagerAdapter(fragmentManager) {

            override fun getItem(position: Int): androidx.fragment.app.Fragment {
                val fragment: TaskRecyclerViewFragment = when (position) {
                    0 -> TaskRecyclerViewFragment.newInstance(context, user, Task.TYPE_HABIT)
                    1 -> TaskRecyclerViewFragment.newInstance(context, user, Task.TYPE_DAILY)
                    3 -> RewardsRecyclerviewFragment.newInstance(context, user, Task.TYPE_REWARD)
                    else -> TaskRecyclerViewFragment.newInstance(context, user, Task.TYPE_TODO)
                }

                viewFragmentsDictionary?.put(position, fragment)

                return fragment
            }

            override fun getCount(): Int = 4

            override fun getPageTitle(position: Int): CharSequence? = when (position) {
                0 -> activity?.getString(R.string.habits)
                1 -> activity?.getString(R.string.dailies)
                2 -> activity?.getString(R.string.todos)
                3 -> activity?.getString(R.string.rewards)
                else -> ""
            }
        }

        viewPager?.addOnPageChangeListener(object : androidx.viewpager.widget.ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                bottomNavigation?.selectTabAtPosition(position)
                updateFilterIcon()
            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        })
    }

    private fun updateFilterIcon() {
        if (filterMenuItem == null) {
            return
        }
        var filterCount = 0
        if (activeFragment != null) {
            filterCount = taskFilterHelper.howMany(activeFragment?.classType)
        }
        if (filterCount == 0) {
            filterMenuItem?.setIcon(R.drawable.ic_action_filter_list)
        } else {
            filterMenuItem?.setIcon(R.drawable.ic_filters_active)
        }
    }

    private fun updateBottomBarBadges() {
        if (bottomNavigation == null) {
            return
        }
        tutorialRepository.getTutorialSteps(Arrays.asList("habits", "dailies", "todos", "rewards")).subscribe(Consumer { tutorialSteps ->
            val activeTutorialFragments = ArrayList<String>()
            for (step in tutorialSteps) {
                var id = -1
                val taskType = when (step.identifier) {
                    "habits" -> {
                        id = R.id.tab_habits
                        Task.TYPE_HABIT
                    }
                    "dailies" -> {
                        id = R.id.tab_dailies
                        Task.TYPE_DAILY
                    }
                    "todos" -> {
                        id = R.id.tab_todos
                        Task.TYPE_TODO
                    }
                    "rewards" -> {
                        id = R.id.tab_rewards
                        Task.TYPE_REWARD
                    }
                    else -> ""
                }
                val tab = bottomNavigation?.getTabWithId(id)
                if (step.shouldDisplay()) {
                    tab?.setBadgeCount(1)
                    activeTutorialFragments.add(taskType)
                } else {
                    tab?.removeBadge()
                }
            }
            if (activeTutorialFragments.size == 1) {
                val fragment = viewFragmentsDictionary?.get(indexForTaskType(activeTutorialFragments[0]))
                if (fragment?.tutorialTexts != null && context != null) {
                    val finalText = context?.getString(R.string.tutorial_tasks_complete)
                    if (!fragment.tutorialTexts.contains(finalText) && finalText != null) {
                        fragment.tutorialTexts.add(finalText)
                    }
                }
            }
        }, RxErrorHandler.handleEmptyError())
    }
    // endregion

    private fun openNewTaskActivity(type: String) {
        if (this.displayingTaskForm) {
            return
        }

        val bundle = Bundle()
        bundle.putString(TaskFormActivity.TASK_TYPE_KEY, type)

        val intent = Intent(activity, TaskFormActivity::class.java)
        intent.putExtras(bundle)
        intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        if (this.isAdded) {
            this.displayingTaskForm = true
            startActivityForResult(intent, TASK_CREATED_RESULT)
        }
    }

    @Subscribe
    fun onEvent(event: TaskTappedEvent) {
        if (this.displayingTaskForm) {
            return
        }

        val bundle = Bundle()
        bundle.putString(TaskFormActivity.TASK_TYPE_KEY, event.Task.type)
        bundle.putString(TaskFormActivity.TASK_ID_KEY, event.Task.id)

        val intent = Intent(activity, TaskFormActivity::class.java)
        intent.putExtras(bundle)
        this.displayingTaskForm = true
        if (isAdded) {
            startActivityForResult(intent, TASK_UPDATED_RESULT)
        }
    }

    //endregion Events

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            TASK_CREATED_RESULT -> {
                this.displayingTaskForm = false
                onTaskCreatedResult(resultCode, data)
            }
            TASK_UPDATED_RESULT -> this.displayingTaskForm = false
        }
        floatingMenu?.close(true)
    }

    private fun onTaskCreatedResult(resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            val taskType = data?.getStringExtra(TaskFormActivity.TASK_TYPE_KEY)
            if (taskType != null) {
                switchToTaskTab(taskType)
            }
        }
    }

    private fun switchToTaskTab(taskType: String) {
        val index = indexForTaskType(taskType)
        if (viewPager != null && index != -1) {
            viewPager?.currentItem = index
            updateBottomBarBadges()
        }
    }

    private fun indexForTaskType(taskType: String?): Int {
        if (taskType != null) {
            for (index in 0 until (viewFragmentsDictionary?.size ?: 0)) {
                val fragment = viewFragmentsDictionary?.get(index)
                if (fragment != null && taskType == fragment.className) {
                    return index
                }
            }
        }
        return -1
    }

    override val displayedClassName: String?
        get() = null

    override fun addToBackStack(): Boolean = false

    companion object {
        private const val TASK_CREATED_RESULT = 1
        private const val TASK_UPDATED_RESULT = 2
    }
}