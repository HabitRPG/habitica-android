package com.habitrpg.android.habitica.ui.fragments.tasks

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.NO_POSITION
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.data.TaskRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.databinding.FragmentRefreshRecyclerviewBinding
import com.habitrpg.android.habitica.extensions.observeOnce
import com.habitrpg.android.habitica.extensions.setScaledPadding
import com.habitrpg.android.habitica.extensions.subscribeWithErrorHandler
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.helpers.HapticFeedbackManager
import com.habitrpg.android.habitica.helpers.MainNavigationController
import com.habitrpg.android.habitica.helpers.NotificationsManager
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.helpers.SoundManager
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.ui.activities.MainActivity
import com.habitrpg.android.habitica.ui.activities.TaskFormActivity
import com.habitrpg.android.habitica.ui.adapter.BaseRecyclerViewAdapter
import com.habitrpg.android.habitica.ui.adapter.tasks.DailiesRecyclerViewHolder
import com.habitrpg.android.habitica.ui.adapter.tasks.HabitsRecyclerViewAdapter
import com.habitrpg.android.habitica.ui.adapter.tasks.RewardsRecyclerViewAdapter
import com.habitrpg.android.habitica.ui.adapter.tasks.TaskRecyclerViewAdapter
import com.habitrpg.android.habitica.ui.adapter.tasks.TodosRecyclerViewAdapter
import com.habitrpg.android.habitica.ui.fragments.BaseFragment
import com.habitrpg.android.habitica.ui.helpers.SafeDefaultItemAnimator
import com.habitrpg.android.habitica.ui.viewHolders.tasks.BaseTaskViewHolder
import com.habitrpg.android.habitica.ui.viewmodels.TasksViewModel
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper
import com.habitrpg.android.habitica.ui.views.HabiticaSnackbar
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaAlertDialog
import com.habitrpg.common.habitica.helpers.EmptyItem
import com.habitrpg.common.habitica.models.responses.TaskDirection
import com.habitrpg.common.habitica.models.responses.TaskScoringResult
import com.habitrpg.common.habitica.models.tasks.TaskType
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.Date
import java.util.concurrent.TimeUnit
import javax.inject.Inject

open class TaskRecyclerViewFragment : BaseFragment<FragmentRefreshRecyclerviewBinding>(), androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener {
    private var taskFlowJob: Job? = null
    val viewModel: TasksViewModel by viewModels({requireParentFragment()})

    internal var canEditTasks: Boolean = true
    internal var canScoreTaks: Boolean = true
    override var binding: FragmentRefreshRecyclerviewBinding? = null

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentRefreshRecyclerviewBinding {
        return FragmentRefreshRecyclerviewBinding.inflate(inflater, container, false)
    }

    private var recyclerSubscription: CompositeDisposable = CompositeDisposable()
    var recyclerAdapter: TaskRecyclerViewAdapter? = null
    var itemAnimator = SafeDefaultItemAnimator()
    @Inject
    lateinit var userRepository: UserRepository
    @Inject
    lateinit var inventoryRepository: InventoryRepository
    @Inject
    lateinit var taskRepository: TaskRepository
    @Inject
    lateinit var soundManager: SoundManager
    @Inject
    lateinit var configManager: AppConfigManager
    @Inject
    lateinit var sharedPreferences: SharedPreferences
    @Inject
    lateinit var notificationsManager: NotificationsManager

    internal var layoutManager: RecyclerView.LayoutManager? = null

    internal var taskType: TaskType = TaskType.HABIT
    private var itemTouchCallback: ItemTouchHelper.Callback? = null

    internal val className: TaskType
        get() = this.taskType

    private fun setInnerAdapter() {
        if (binding?.recyclerView?.adapter != null && binding?.recyclerView?.adapter == recyclerAdapter && !recyclerSubscription.isDisposed) {
            return
        }
        if (!recyclerSubscription.isDisposed) {
            recyclerSubscription.dispose()
        }
        recyclerSubscription = CompositeDisposable()
        viewModel.let { viewModel ->
            val adapter: BaseRecyclerViewAdapter<*, *>? = when (this.taskType) {
                TaskType.HABIT -> HabitsRecyclerViewAdapter(R.layout.habit_item_card, viewModel)
                TaskType.DAILY -> DailiesRecyclerViewHolder(R.layout.daily_item_card, viewModel)
                TaskType.TODO -> TodosRecyclerViewAdapter(R.layout.todo_item_card, viewModel)
                TaskType.REWARD -> RewardsRecyclerViewAdapter(null, R.layout.reward_item_card)
                else -> null
            }

            recyclerAdapter = adapter as? TaskRecyclerViewAdapter
            recyclerAdapter?.canScoreTasks = canScoreTaks
            binding?.recyclerView?.adapter = adapter

            viewModel.getFilterSet(taskType)?.observe(viewLifecycleOwner) {
                recyclerAdapter?.filter()
            }
        }
        context?.let { recyclerAdapter?.taskDisplayMode = configManager.taskDisplayMode(it) }

        recyclerAdapter?.errorButtonEvents?.subscribe(
            {
                taskRepository.syncErroredTasks().subscribe({}, RxErrorHandler.handleEmptyError())
            },
            RxErrorHandler.handleEmptyError()
        )?.let { recyclerSubscription.add(it) }
        recyclerAdapter?.taskOpenEvents?.subscribeWithErrorHandler {
            openTaskForm(it.first)
        }?.let { recyclerSubscription.add(it) }
        recyclerAdapter?.taskScoreEvents
            ?.doOnNext {
                playSound(it.second)
                context?.let { it1 -> notificationsManager.dismissTaskNotification(it1, it.first) }
            }?.subscribeWithErrorHandler { scoreTask(it.first, it.second) }?.let { recyclerSubscription.add(it) }
        recyclerAdapter?.checklistItemScoreEvents
            ?.flatMap {
                taskRepository.scoreChecklistItem(it.first.id ?: "", it.second.id ?: "")
            }?.subscribeWithErrorHandler {}?.let { recyclerSubscription.add(it) }
        recyclerAdapter?.brokenTaskEvents?.subscribeWithErrorHandler { showBrokenChallengeDialog(it) }?.let { recyclerSubscription.add(it) }
        recyclerAdapter?.adventureGuideOpenEvents?.subscribeWithErrorHandler { MainNavigationController.navigate(R.id.adventureGuideActivity) }?.let { recyclerSubscription.add(it) }

        viewModel.ownerID.observe(viewLifecycleOwner) {
            canEditTasks = viewModel.isPersonalBoard ?: true
            canScoreTaks = viewModel.isPersonalBoard ?: true
            recyclerAdapter?.canScoreTasks = canScoreTaks
            updateTaskSubscription(it)
        }
    }

    private fun handleTaskResult(result: TaskScoringResult, value: Int) {
        if (taskType == TaskType.REWARD) {
            (activity as? MainActivity)?.let { activity ->
                HabiticaSnackbar.showSnackbar(
                    activity.snackbarContainer, null, getString(R.string.notification_purchase_reward),
                    BitmapDrawable(resources, HabiticaIconsHelper.imageOfGold()),
                    ContextCompat.getColor(activity, R.color.yellow_10),
                    "-$value",
                    HabiticaSnackbar.SnackbarDisplayType.DROP
                )
            }
        } else {
            (activity as? MainActivity)?.displayTaskScoringResponse(result)
        }
    }

    private fun playSound(direction: TaskDirection) {
        HapticFeedbackManager.tap(requireView())
        val soundName = when (taskType) {
            TaskType.HABIT -> if (direction == TaskDirection.UP) SoundManager.SoundPlusHabit else SoundManager.SoundMinusHabit
            TaskType.DAILY -> SoundManager.SoundDaily
            TaskType.TODO -> SoundManager.SoundTodo
            TaskType.REWARD -> SoundManager.SoundReward
            else -> null
        }
        soundName?.let { soundManager.loadAndPlayAudio(it) }
    }

    private fun allowReordering() {
        val itemTouchHelper = itemTouchCallback?.let { ItemTouchHelper(it) }
        itemTouchHelper?.attachToRecyclerView(binding?.recyclerView)
    }

    protected open fun getLayoutManager(context: Context?): androidx.recyclerview.widget.LinearLayoutManager {
        return androidx.recyclerview.widget.LinearLayoutManager(context)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        itemTouchCallback = null
    }

    override fun onDestroy() {
        userRepository.close()
        super.onDestroy()
    }

    override fun injectFragment(component: UserComponent) {
        component.inject(this)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        savedInstanceState?.let { this.taskType = TaskType.from(savedInstanceState.getString(CLASS_TYPE_KEY, "")) ?: TaskType.HABIT }

        this.setInnerAdapter()
        recyclerAdapter?.filter()

        itemTouchCallback = object : ItemTouchHelper.Callback() {
            override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                super.onSelectedChanged(viewHolder, actionState)
                if (viewHolder == null || viewHolder.bindingAdapterPosition == NO_POSITION) return
                val taskViewHolder = viewHolder as? BaseTaskViewHolder
                if (taskViewHolder != null) {
                    taskViewHolder.movingFromPosition = viewHolder.bindingAdapterPosition
                }
                binding?.refreshLayout?.isEnabled = false
            }

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                recyclerAdapter?.notifyItemMoved(viewHolder.bindingAdapterPosition, target.bindingAdapterPosition)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) { /* no-on */ }

            // defines the enabled move directions in each state (idle, swiping, dragging).
            override fun getMovementFlags(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ): Int {
                return if (recyclerAdapter?.getItemViewType(viewHolder.bindingAdapterPosition) ?: 0 != 0) {
                    makeFlag(ItemTouchHelper.ACTION_STATE_IDLE, 0)
                } else {
                    makeFlag(
                        ItemTouchHelper.ACTION_STATE_DRAG,
                        ItemTouchHelper.DOWN or ItemTouchHelper.UP
                    )
                }
            }

            override fun isItemViewSwipeEnabled(): Boolean = false

            override fun isLongPressDragEnabled(): Boolean = true

            override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                super.clearView(recyclerView, viewHolder)
                binding?.refreshLayout?.isEnabled = true

                if (viewHolder.bindingAdapterPosition == NO_POSITION) return
                val taskViewHolder = viewHolder as? BaseTaskViewHolder
                val validTaskId = taskViewHolder?.task?.takeIf { it.isValid }?.id
                if (viewHolder.bindingAdapterPosition != taskViewHolder?.movingFromPosition) {
                    taskViewHolder?.movingFromPosition = null
                    updateTaskInRepository(validTaskId, viewHolder)
                }
            }

            private fun updateTaskInRepository(
                validTaskId: String?,
                viewHolder: RecyclerView.ViewHolder
            ) {
                if (validTaskId != null) {
                    var newPosition = viewHolder.bindingAdapterPosition
                    if ((viewModel.filterCount(taskType) ?: 0) > 0) {
                        newPosition = if ((newPosition + 1) == recyclerAdapter?.data?.size) {
                            recyclerAdapter?.data?.get(newPosition - 1)?.position ?: newPosition
                        } else {
                            (recyclerAdapter?.data?.get(newPosition + 1)?.position ?: newPosition) - 1
                        }
                    }
                    compositeSubscription.add(
                        taskRepository.updateTaskPosition(
                            taskType, validTaskId, newPosition
                        )
                            .delay(1, TimeUnit.SECONDS)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                {
                                },
                                RxErrorHandler.handleEmptyError()
                            )
                    )
                }
            }
        }

        binding?.recyclerView?.setScaledPadding(context, 0, 0, 0, 108)

        layoutManager = getLayoutManager(context)
        binding?.recyclerView?.layoutManager = layoutManager

        allowReordering()

        binding?.recyclerView?.itemAnimator = itemAnimator

        binding?.refreshLayout?.setOnRefreshListener(this)

        setEmptyLabels()

        binding?.recyclerView?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    binding?.refreshLayout?.isEnabled = (activity as? MainActivity)?.isAppBarExpanded ?: false
                }
            }
        })

        compositeSubscription.add(
            userRepository.getUserFlowable()
                .distinct { it.hasCompletedOnboarding }
                .doOnNext { recyclerAdapter?.showAdventureGuide = !it.hasCompletedOnboarding }
                .subscribe({ recyclerAdapter?.user = it }, RxErrorHandler.handleEmptyError())
        )
    }

    private fun updateTaskSubscription(ownerID: String?) {
        if (taskFlowJob?.isActive == true) {
            taskFlowJob?.cancel()
        }
        taskFlowJob = lifecycleScope.launch {
            taskRepository.getTasks(taskType, ownerID).collect {
                recyclerAdapter?.updateUnfilteredData(it)
            }
        }
    }

    protected fun showBrokenChallengeDialog(task: Task) {
        context?.let {
            if (!task.isValid) {
                return
            }
            taskRepository.getTasksForChallenge(task.challengeID).firstElement().subscribe(
                { tasks ->
                    val taskCount = tasks.size
                    val dialog = HabiticaAlertDialog(it)
                    dialog.setTitle(R.string.broken_challenge)
                    dialog.setMessage(it.getString(R.string.broken_challenge_description, taskCount))
                    dialog.addButton(it.getString(R.string.keep_x_tasks, taskCount), true) { _, _ ->
                        if (!task.isValid) return@addButton
                        taskRepository.unlinkAllTasks(task.challengeID, "keep-all")
                            .flatMap { userRepository.retrieveUser(true, forced = true) }
                            .subscribe({}, RxErrorHandler.handleEmptyError())
                    }
                    dialog.addButton(
                        it.getString(R.string.delete_x_tasks, taskCount),
                        isPrimary = false,
                        isDestructive = true
                    ) { _, _ ->
                        if (!task.isValid) return@addButton
                        taskRepository.unlinkAllTasks(task.challengeID, "remove-all")
                            .flatMap { userRepository.retrieveUser(true, forced = true) }
                            .subscribe({}, RxErrorHandler.handleEmptyError())
                    }
                    dialog.setExtraCloseButtonVisibility(View.VISIBLE)
                    dialog.show()
                },
                RxErrorHandler.handleEmptyError()
            )
        }
    }

    private fun setEmptyLabels() {
        binding?.recyclerView?.emptyItem = if ((viewModel.filterCount(taskType) ?: 0) > 0) {
            when (this.taskType) {
                TaskType.HABIT -> {
                    EmptyItem(
                        getString(R.string.empty_title_habits_filtered),
                        getString(R.string.empty_description_habits_filtered),
                        R.drawable.icon_habits
                    )
                }
                TaskType.DAILY -> {
                    EmptyItem(
                        getString(R.string.empty_title_dailies_filtered),
                        getString(R.string.empty_description_dailies_filtered),
                        R.drawable.icon_dailies
                    )
                }
                TaskType.TODO -> {
                    EmptyItem(
                        getString(R.string.empty_title_todos_filtered),
                        getString(R.string.empty_description_todos_filtered),
                        R.drawable.icon_todos
                    )
                }
                TaskType.REWARD -> {
                    EmptyItem(
                        getString(R.string.empty_title_rewards_filtered),
                        null,
                        R.drawable.icon_rewards
                    )
                }
                else -> EmptyItem("")
            }
        } else {
            when (this.taskType) {
                TaskType.HABIT -> {
                    EmptyItem(
                        getString(R.string.empty_title_habits),
                        getString(R.string.empty_description_habits),
                        R.drawable.icon_habits
                    )
                }
                TaskType.DAILY -> {
                    EmptyItem(
                        getString(R.string.empty_title_dailies),
                        getString(R.string.empty_description_dailies),
                        R.drawable.icon_dailies
                    )
                }
                TaskType.TODO -> {
                    EmptyItem(
                        getString(R.string.empty_title_todos),
                        getString(R.string.empty_description_todos),
                        R.drawable.icon_todos
                    )
                }
                TaskType.REWARD -> {
                    EmptyItem(
                        getString(R.string.empty_title_rewards),
                        null,
                        R.drawable.icon_rewards
                    )
                }
                else -> EmptyItem("")
            }
        }
    }

    private fun scoreTask(task: Task, direction: TaskDirection) {
        viewModel.scoreTask(task, direction) { result, value ->
            handleTaskResult(result, value)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(CLASS_TYPE_KEY, this.taskType.value)
    }

    override val displayedClassName: String?
        get() = this.taskType.value + super.displayedClassName

    override fun onRefresh() {
        binding?.refreshLayout?.isRefreshing = true
        viewModel.refreshData {
            binding?.refreshLayout?.isRefreshing = false
        }
    }

    override fun onStart() {
        super.onStart()
        (activity as? MainActivity)?.viewModel?.user?.observeOnce(this) {
            if (it != null) {
                when (taskType) {
                    TaskType.TODO -> viewModel.setActiveFilter(
                        TaskType.TODO,
                        Task.FILTER_ACTIVE
                    )
                    TaskType.DAILY -> {
                        if (it.isValid && it.preferences?.dailyDueDefaultView == true) {
                            viewModel.setActiveFilter(TaskType.DAILY, Task.FILTER_ACTIVE)
                        }
                    }
                    else -> {}
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        context?.let { recyclerAdapter?.taskDisplayMode = configManager.taskDisplayMode(it) }
        setInnerAdapter()
        recyclerAdapter?.filter()
    }

    fun setActiveFilter(activeFilter: String) {
        viewModel.setActiveFilter(taskType, activeFilter)
        recyclerAdapter?.filter()

        setEmptyLabels()

        if (activeFilter == Task.FILTER_COMPLETED) {
            compositeSubscription.add(taskRepository.retrieveCompletedTodos().subscribe({}, RxErrorHandler.handleEmptyError()))
        }
    }

    private fun openTaskForm(task: Task) {
        if (Date().time - (TasksFragment.lastTaskFormOpen?.time ?: 0) < 2000 || !task.isValid || !canEditTasks) {
            return
        }

        val bundle = Bundle()
        bundle.putString(TaskFormActivity.TASK_TYPE_KEY, task.type?.value)
        bundle.putString(TaskFormActivity.TASK_ID_KEY, task.id)
        bundle.putDouble(TaskFormActivity.TASK_VALUE_KEY, task.value)

        val intent = Intent(activity, TaskFormActivity::class.java)
        intent.putExtras(bundle)
        TasksFragment.lastTaskFormOpen = Date()
        if (isAdded) {
            startActivity(intent)
        }
    }

    companion object {
        private const val CLASS_TYPE_KEY = "CLASS_TYPE_KEY"

        fun newInstance(context: Context?, classType: TaskType): TaskRecyclerViewFragment {
            val fragment = TaskRecyclerViewFragment()
            fragment.taskType = classType
            var tutorialTexts: List<String>? = null
            if (context != null) {
                when (fragment.taskType) {
                    TaskType.HABIT -> {
                        fragment.tutorialStepIdentifier = "habits"
                        tutorialTexts = listOf(context.getString(R.string.tutorial_overview), context.getString(R.string.tutorial_habits_1), context.getString(R.string.tutorial_habits_2), context.getString(R.string.tutorial_habits_3), context.getString(R.string.tutorial_habits_4))
                    }
                    TaskType.DAILY -> {
                        fragment.tutorialStepIdentifier = "dailies"
                        tutorialTexts = listOf(context.getString(R.string.tutorial_dailies_1), context.getString(R.string.tutorial_dailies_2))
                    }
                    TaskType.TODO -> {
                        fragment.tutorialStepIdentifier = "todos"
                        tutorialTexts = listOf(context.getString(R.string.tutorial_todos_1), context.getString(R.string.tutorial_todos_2))
                    }
                    TaskType.REWARD -> {
                        fragment.tutorialStepIdentifier = "rewards"
                        tutorialTexts = listOf(context.getString(R.string.tutorial_rewards_1), context.getString(R.string.tutorial_rewards_2))
                    }
                }
            }

            if (tutorialTexts != null) {
                fragment.tutorialTexts = ArrayList(tutorialTexts)
            }
            fragment.tutorialCanBeDeferred = false

            return fragment
        }
    }
}
