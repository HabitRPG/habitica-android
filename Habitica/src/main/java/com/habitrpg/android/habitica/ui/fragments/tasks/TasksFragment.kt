package com.habitrpg.android.habitica.ui.fragments.tasks

import android.app.Activity
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentPagerAdapter
import com.habitrpg.android.habitica.HabiticaBaseApplication
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.TagRepository
import com.habitrpg.android.habitica.databinding.FragmentViewpagerBinding
import com.habitrpg.android.habitica.extensions.getThemeColor
import com.habitrpg.android.habitica.extensions.setTintWith
import com.habitrpg.android.habitica.helpers.AmplitudeManager
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.helpers.TaskFilterHelper
import com.habitrpg.android.habitica.ui.activities.TaskFormActivity
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment
import com.habitrpg.android.habitica.ui.views.tasks.TaskFilterDialog
import com.habitrpg.shared.habitica.models.tasks.TaskType
import com.habitrpg.shared.habitica.models.user.User
import io.reactivex.disposables.Disposable
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList


class TasksFragment : BaseMainFragment<FragmentViewpagerBinding>(), SearchView.OnQueryTextListener {

    override var binding: FragmentViewpagerBinding? = null

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentViewpagerBinding {
        return FragmentViewpagerBinding.inflate(inflater, container, false)
    }

    @Inject
    lateinit var taskFilterHelper: TaskFilterHelper
    @Inject
    lateinit var tagRepository: TagRepository
    @Inject
    lateinit var appConfigManager: AppConfigManager

    private var refreshItem: MenuItem? = null
    internal var viewFragmentsDictionary: MutableMap<Int, TaskRecyclerViewFragment>? = WeakHashMap()

    private var filterMenuItem: MenuItem? = null

    override var user: User?
        get() = super.user
        set(value) {
            super.user = value
            viewFragmentsDictionary?.values?.forEach { it.user = value }
        }

    private val activeFragment: TaskRecyclerViewFragment?
        get() {
            var fragment = viewFragmentsDictionary?.get(binding?.viewPager?.currentItem)
            if (fragment == null) {
                if (isAdded) {
                    fragment = (childFragmentManager.findFragmentByTag("android:switcher:" + R.id.viewPager + ":" + binding?.viewPager?.currentItem) as? TaskRecyclerViewFragment)
                }
            }
            return fragment
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        this.usesTabLayout = false
        this.usesBottomNavigation = true
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadTaskLists()
    }

    override fun onResume() {
        super.onResume()

        bottomNavigation?.onTabSelectedListener = {
            val newItem = when (it) {
                TaskType.TYPE_HABIT -> 0
                TaskType.TYPE_DAILY -> 1
                TaskType.TYPE_TODO -> 2
                TaskType.TYPE_REWARD -> 3
                else -> 0
            }
            binding?.viewPager?.currentItem = newItem
            updateBottomBarBadges()
        }
        bottomNavigation?.onAddListener = { type ->
            openNewTaskActivity(type)
        }
        bottomNavigation?.flipAddBehaviour = appConfigManager.flipAddTaskBehaviour()
    }

    override fun onPause() {
        bottomNavigation?.onTabSelectedListener = null
        bottomNavigation?.onAddListener = null

        super.onPause()
    }

    override fun onDestroy() {
        tagRepository.close()
        super.onDestroy()
    }

    override fun injectFragment(component: UserComponent) {
        component.inject(this)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main_activity, menu)

        filterMenuItem = menu.findItem(R.id.action_filter)
        updateFilterIcon()

        val item = menu.findItem(R.id.action_search)
        tintMenuIcon(item)
        val sv = item.actionView as? SearchView
        sv?.setOnQueryTextListener(this)
        sv?.setIconifiedByDefault(false)
        item.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                filterMenuItem?.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                return true
            }

            override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                // Do something when expanded
                filterMenuItem?.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                return true
            }
        })
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        taskFilterHelper.searchQuery = newText
        viewFragmentsDictionary?.values?.forEach { values -> values.recyclerAdapter?.filter() }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_filter -> {
                showFilterDialog()
                true
            }
            R.id.action_reload -> {
                refreshItem = item
                refresh()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showFilterDialog() {
        context?.let {
            var disposable: Disposable? = null
            val dialog = TaskFilterDialog(it, HabiticaBaseApplication.userComponent)
            if (user != null) {
                dialog.setTags(user?.tags?.createSnapshot() ?: emptyList())
                disposable = tagRepository.getTags(user?.id ?: "").subscribe({ tagsList -> dialog.setTags(tagsList)}, RxErrorHandler.handleEmptyError())
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
                    taskFilterHelper.tags = activeTags
                    if (activeTaskFilter != null) {
                        activeFragment?.setActiveFilter(activeTaskFilter)
                    }
                    viewFragmentsDictionary?.values?.forEach { values -> values.recyclerAdapter?.filter() }
                    updateFilterIcon()
                }
            })
            dialog.setOnDismissListener {
                if (disposable?.isDisposed == false) {
                    disposable.dispose()
                }
            }
            dialog.show()
        }
    }

    private fun refresh() {
        activeFragment?.onRefresh()
    }

    private fun loadTaskLists() {
        val fragmentManager = childFragmentManager

        binding?.viewPager?.adapter = object : FragmentPagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

            override fun getItem(position: Int): androidx.fragment.app.Fragment {
                val fragment: TaskRecyclerViewFragment = when (position) {
                    0 -> TaskRecyclerViewFragment.newInstance(context, user, TaskType.TYPE_HABIT)
                    1 -> TaskRecyclerViewFragment.newInstance(context, user, TaskType.TYPE_DAILY)
                    3 -> RewardsRecyclerviewFragment.newInstance(context, user, TaskType.TYPE_REWARD)
                    else -> TaskRecyclerViewFragment.newInstance(context, user, TaskType.TYPE_TODO)
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

        binding?.viewPager?.addOnPageChangeListener(object : androidx.viewpager.widget.ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) { /* no-op */ }

            override fun onPageSelected(position: Int) {
                bottomNavigation?.selectedPosition = position
                updateFilterIcon()
            }

            override fun onPageScrollStateChanged(state: Int) { /* no-op */ }
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
            context?.let {
                val filterIcon = ContextCompat.getDrawable(it, R.drawable.ic_action_filter_list)
                filterIcon?.setTintWith(it.getThemeColor(R.attr.headerTextColor), PorterDuff.Mode.MULTIPLY)
                filterMenuItem?.setIcon(filterIcon)
            }
        } else {
            context?.let {
                val filterIcon = ContextCompat.getDrawable(it, R.drawable.ic_filters_active)
                filterIcon?.setTintWith(it.getThemeColor(R.attr.textColorPrimaryDark), PorterDuff.Mode.MULTIPLY)
                filterMenuItem?.setIcon(filterIcon)
            }
        }
    }

    private fun updateBottomBarBadges() {
        if (bottomNavigation == null) {
            return
        }
        compositeSubscription.add(tutorialRepository.getTutorialSteps(listOf("habits", "dailies", "todos", "rewards")).subscribe({ tutorialSteps ->
            val activeTutorialFragments = ArrayList<String>()
            for (step in tutorialSteps) {
                var id = -1
                val taskType = when (step.identifier) {
                    "habits" -> {
                        id = R.id.habits_tab
                        TaskType.TYPE_HABIT
                    }
                    "dailies" -> {
                        id = R.id.dailies_tab
                        TaskType.TYPE_DAILY
                    }
                    "todos" -> {
                        id = R.id.todos_tab
                        TaskType.TYPE_TODO
                    }
                    "rewards" -> {
                        id = R.id.rewards_tab
                        Task.TYPE_REWARD
                    }
                    else -> ""
                }
                val tab = bottomNavigation?.tabWithId(id)
                if (step.shouldDisplay()) {
                    tab?.badgeCount = 1
                    activeTutorialFragments.add(taskType)
                } else {
                    tab?.badgeCount = 0
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
        }, RxErrorHandler.handleEmptyError()))
    }
    // endregion

    private fun openNewTaskActivity(type: String) {
        if (Date().time - (lastTaskFormOpen?.time ?: 0) < 2000) {
            return
        }

        val additionalData = HashMap<String, Any>()
        additionalData["created task type"] = type
        additionalData["viewed task type"] = when (binding?.viewPager?.currentItem) {
            0 -> TaskType.TYPE_HABIT
            1 -> TaskType.TYPE_DAILY
            2 -> TaskType.TYPE_TODO
            3 -> Task.TYPE_REWARD
            else -> ""
        }
        AmplitudeManager.sendEvent("open create task form", AmplitudeManager.EVENT_CATEGORY_BEHAVIOUR, AmplitudeManager.EVENT_HITTYPE_EVENT, additionalData)

        val bundle = Bundle()
        bundle.putString(TaskFormActivity.TASK_TYPE_KEY, type)
        bundle.putStringArrayList(TaskFormActivity.SELECTED_TAGS_KEY, ArrayList(taskFilterHelper.tags))

        val intent = Intent(activity, TaskFormActivity::class.java)
        intent.putExtras(bundle)
        intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        if (this.isAdded) {
            lastTaskFormOpen = Date()
            startActivityForResult(intent, TASK_CREATED_RESULT)
        }
    }

    //endregion Events

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            TASK_CREATED_RESULT -> {
                onTaskCreatedResult(resultCode, data)
            }
        }
    }

    private fun onTaskCreatedResult(resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            val taskType = data?.getStringExtra(TaskFormActivity.TASK_TYPE_KEY)
            if (taskType != null) {
                switchToTaskTab(taskType)

                val index = indexForTaskType(taskType)
                if (index != -1) {
                    val fragment = viewFragmentsDictionary?.get(index)
                    fragment?.binding?.recyclerView?.scrollToPosition(0)
                }
            }
        }
    }

    private fun switchToTaskTab(taskType: String) {
        val index = indexForTaskType(taskType)
        if (binding?.viewPager != null && index != -1) {
            binding?.viewPager?.currentItem = index
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
        var lastTaskFormOpen: Date? = null
        internal const val TASK_CREATED_RESULT = 1
        const val TASK_UPDATED_RESULT = 2
    }
}