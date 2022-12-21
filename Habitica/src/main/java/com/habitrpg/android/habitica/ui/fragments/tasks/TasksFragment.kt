package com.habitrpg.android.habitica.ui.fragments.tasks

import android.app.Activity
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.habitrpg.android.habitica.HabiticaBaseApplication
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.databinding.FragmentViewpagerBinding
import com.habitrpg.android.habitica.extensions.setTintWith
import com.habitrpg.android.habitica.helpers.AmplitudeManager
import com.habitrpg.android.habitica.helpers.MainNavigationController
import com.habitrpg.android.habitica.helpers.launchCatching
import com.habitrpg.android.habitica.ui.activities.TaskFormActivity
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment
import com.habitrpg.android.habitica.ui.viewmodels.TasksViewModel
import com.habitrpg.android.habitica.ui.views.navigation.HabiticaBottomNavigationViewListener
import com.habitrpg.android.habitica.ui.views.tasks.TaskFilterDialog
import com.habitrpg.common.habitica.extensions.getThemeColor
import com.habitrpg.shared.habitica.models.tasks.TaskType
import java.util.Date
import java.util.WeakHashMap

class TasksFragment : BaseMainFragment<FragmentViewpagerBinding>(), SearchView.OnQueryTextListener, HabiticaBottomNavigationViewListener {
    internal val viewModel: TasksViewModel by viewModels()
    override var binding: FragmentViewpagerBinding? = null

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentViewpagerBinding {
        return FragmentViewpagerBinding.inflate(inflater, container, false)
    }

    private var refreshItem: MenuItem? = null
    internal var viewFragmentsDictionary: MutableMap<Int, TaskRecyclerViewFragment>? = WeakHashMap()

    private var filterMenuItem: MenuItem? = null

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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        this.usesTabLayout = false
        this.usesBottomNavigation = true
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadTaskLists()
        arguments?.let {
            val args = TasksFragmentArgs.fromBundle(it)
            val taskTypeValue = args.taskType
            if (args.ownerID?.isNotBlank() == true) {
                viewModel.canSwitchOwners.value = false
                viewModel.ownerID.value = args.ownerID ?: viewModel.userViewModel.userID
            } else if (viewModel.ownerID.value?.isNotBlank() != true) {
                viewModel.ownerID.value = viewModel.userViewModel.userID
            }
            if (taskTypeValue?.isNotBlank() == true) {
                val taskType = TaskType.from(taskTypeValue)
                switchToTaskTab(taskType)
            } else {
                when (viewModel.sharedPreferences.getString("launch_screen", "")) {
                    "/user/tasks/habits" -> onTabSelected(TaskType.HABIT, false)
                    "/user/tasks/dailies" -> onTabSelected(TaskType.DAILY, false)
                    "/user/tasks/todos" -> onTabSelected(TaskType.TODO, false)
                    "/user/tasks/rewards" -> onTabSelected(TaskType.REWARD, false)
                }
            }
        }

        viewModel.ownerID.observe(viewLifecycleOwner) {
            updateBoardDisplay()
        }
        viewModel.canSwitchOwners.observe(viewLifecycleOwner) {
            isTitleInteractive = it ?: false
            updateToolbarInteractivity()
        }
    }

    override fun onResume() {
        super.onResume()
        bottomNavigation?.activeTaskType = when (binding?.viewPager?.currentItem) {
            0 -> TaskType.HABIT
            1 -> TaskType.DAILY
            2 -> TaskType.TODO
            3 -> TaskType.REWARD
            else -> TaskType.HABIT
        }
        binding?.viewPager?.currentItem = binding?.viewPager?.currentItem ?: 0
        bottomNavigation?.listener = this
        lifecycleScope.launchCatching {
            bottomNavigation?.canAddTasks = viewModel.canAddTasks()
        }

        activity?.binding?.content?.toolbarTitle?.setOnClickListener {
            viewModel.cycleOwnerIDs()
        }
    }

    override fun onPause() {
        if (bottomNavigation?.listener == this) {
            bottomNavigation?.listener = null
        }
        super.onPause()
    }

    override fun injectFragment(component: UserComponent) {
        component.inject(this)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        if (viewModel.isPersonalBoard) {
            inflater.inflate(R.menu.menu_main_activity, menu)
        } else {
            inflater.inflate(R.menu.menu_team_board, menu)
        }

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
        viewModel.searchQuery = newText
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
                viewModel.refreshData { }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showFilterDialog() {
        context?.let {
            val dialog = TaskFilterDialog(it, HabiticaBaseApplication.userComponent, viewModel.isPersonalBoard)
            dialog.viewModel = viewModel

            // There are some cases where these things might not be correctly set after the app resumes. This is just to catch that as best as possible
            val navigation = bottomNavigation ?: activity?.binding?.content?.bottomNavigation
            val taskType = navigation?.activeTaskType ?: activeFragment?.taskType

            dialog.setOnDismissListener { updateFilterIcon() }

            if (taskType != null) {
                dialog.taskType = taskType
            }
            dialog.show()
        }
    }

    private fun loadTaskLists() {
        val fragmentManager = childFragmentManager

        binding?.viewPager?.adapter = object : FragmentStateAdapter(fragmentManager, lifecycle) {
            override fun createFragment(position: Int): Fragment {
                val fragment: TaskRecyclerViewFragment = when (position) {
                    0 -> TaskRecyclerViewFragment.newInstance(context, TaskType.HABIT)
                    1 -> TaskRecyclerViewFragment.newInstance(context, TaskType.DAILY)
                    3 -> RewardsRecyclerviewFragment.newInstance(context, TaskType.REWARD, true)
                    else -> TaskRecyclerViewFragment.newInstance(context, TaskType.TODO)
                }
                viewFragmentsDictionary?.put(position, fragment)
                return fragment
            }

            override fun getItemCount(): Int = 4
        }

        binding?.viewPager?.registerOnPageChangeCallback(object :
                ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    bottomNavigation?.selectedPosition = position
                    updateFilterIcon()
                }
            })
    }

    private fun updateFilterIcon() {
        val filterCount = viewModel.filterCount(activeFragment?.taskType)

        filterMenuItem?.isVisible = activeFragment?.taskType != TaskType.REWARD
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
        lifecycleScope.launchCatching {
            tutorialRepository.getTutorialSteps(listOf("habits", "dailies", "todos", "rewards"))
                .collect { tutorialSteps ->
                    val activeTutorialFragments = ArrayList<TaskType>()
                    for (step in tutorialSteps) {
                        var id = -1
                        val taskType = when (step.identifier) {
                            "habits" -> {
                                id = R.id.habits_tab
                                TaskType.HABIT
                            }
                            "dailies" -> {
                                id = R.id.dailies_tab
                                TaskType.DAILY
                            }
                            "todos" -> {
                                id = R.id.todos_tab
                                TaskType.TODO
                            }
                            "rewards" -> {
                                id = R.id.rewards_tab
                                TaskType.REWARD
                            }
                            else -> TaskType.HABIT
                        }
                        val tab = bottomNavigation?.tabWithId(id)
                        if (step.shouldDisplay) {
                            tab?.badgeCount = 1
                            activeTutorialFragments.add(taskType)
                        } else {
                            tab?.badgeCount = 0
                        }
                    }
                    if (activeTutorialFragments.size == 1) {
                        val fragment =
                            viewFragmentsDictionary?.get(indexForTaskType(activeTutorialFragments[0]))
                        if (fragment?.tutorialTexts != null && context != null) {
                            val finalText = context?.getString(R.string.tutorial_tasks_complete)
                            if (!fragment.tutorialTexts.contains(finalText) && finalText != null) {
                                fragment.tutorialTexts = fragment.tutorialTexts + finalText
                            }
                        }
                    }
                }
        }
    }
    // endregion

    private fun openNewTaskActivity(type: TaskType) {
        if (Date().time - (lastTaskFormOpen?.time ?: 0) < 2000) {
            return
        }

        val additionalData = HashMap<String, Any>()
        additionalData["created task type"] = type
        additionalData["viewed task type"] = when (binding?.viewPager?.currentItem) {
            0 -> TaskType.HABIT
            1 -> TaskType.DAILY
            2 -> TaskType.TODO
            3 -> TaskType.REWARD
            else -> ""
        }
        AmplitudeManager.sendEvent("open create task form", AmplitudeManager.EVENT_CATEGORY_BEHAVIOUR, AmplitudeManager.EVENT_HITTYPE_EVENT, additionalData)

        val bundle = Bundle()
        bundle.putString(TaskFormActivity.TASK_TYPE_KEY, type.value)
        if (!viewModel.isPersonalBoard) {
            bundle.putString(TaskFormActivity.GROUP_ID_KEY, viewModel.ownerID.value)
        }
        bundle.putStringArrayList(TaskFormActivity.SELECTED_TAGS_KEY, ArrayList(viewModel.tags))

        val intent = Intent(activity, TaskFormActivity::class.java)
        intent.putExtras(bundle)
        intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        if (this.isAdded) {
            lastTaskFormOpen = Date()
            taskCreateResult.launch(intent)
        }
    }

    //endregion Events

    private val taskCreateResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        onTaskCreatedResult(it.resultCode, it.data)
    }
    private fun onTaskCreatedResult(resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            val taskTypeValue = data?.getStringExtra(TaskFormActivity.TASK_TYPE_KEY)
            if (taskTypeValue != null) {
                val taskType = TaskType.from(taskTypeValue)
                switchToTaskTab(taskType)

                val index = indexForTaskType(taskType)
                if (index != -1) {
                    val fragment = viewFragmentsDictionary?.get(index)
                    fragment?.binding?.recyclerView?.scrollToPosition(0)
                }
            }

            if (!DateUtils.isToday(viewModel.sharedPreferences.getLong("last_creation_reporting", 0))) {
                AmplitudeManager.sendEvent(
                    "task created",
                    AmplitudeManager.EVENT_CATEGORY_BEHAVIOUR,
                    AmplitudeManager.EVENT_HITTYPE_EVENT
                )
                viewModel.sharedPreferences.edit {
                    putLong("last_creation_reporting", Date().time)
                }
            }
        }
    }

    private fun switchToTaskTab(taskType: TaskType?) {
        val index = indexForTaskType(taskType)
        if (binding?.viewPager != null && index != -1) {
            binding?.viewPager?.currentItem = index
            updateBottomBarBadges()
        }
    }

    private fun indexForTaskType(taskType: TaskType?): Int {
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
    }

    override fun onTabSelected(taskType: TaskType, smooth: Boolean) {
        val newItem = when (taskType) {
            TaskType.HABIT -> 0
            TaskType.DAILY -> 1
            TaskType.TODO -> 2
            TaskType.REWARD -> 3
            else -> 0
        }
        binding?.viewPager?.setCurrentItem(newItem, smooth)
        updateBottomBarBadges()
    }

    override fun onAdd(taskType: TaskType) {
        openNewTaskActivity(taskType)
    }

    private fun updateBoardDisplay() {
        if (viewModel.ownerTitle.isNotBlank()) {
            activity?.title = viewModel.ownerTitle
            MainNavigationController.updateLabel(R.id.tasksFragment, viewModel.ownerTitle.toString())
        }
        viewModel.userViewModel.currentTeamPlan.value = viewModel.teamPlans[viewModel.ownerID.value]
        lifecycleScope.launchCatching {
            bottomNavigation?.canAddTasks = viewModel.canAddTasks()
        }
    }
}
