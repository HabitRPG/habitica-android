package com.habitrpg.android.habitica.ui.fragments.tasks

import android.content.Context
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.NO_POSITION
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.data.TaskRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.databinding.FragmentRefreshRecyclerviewBinding
import com.habitrpg.android.habitica.extensions.setScaledPadding
import com.habitrpg.android.habitica.extensions.subscribeWithErrorHandler
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.helpers.SoundManager
import com.habitrpg.android.habitica.helpers.TaskFilterHelper
import com.habitrpg.android.habitica.models.responses.TaskDirection
import com.habitrpg.android.habitica.models.responses.TaskScoringResult
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.ui.activities.MainActivity
import com.habitrpg.android.habitica.ui.activities.TaskFormActivity
import com.habitrpg.android.habitica.ui.adapter.BaseRecyclerViewAdapter
import com.habitrpg.android.habitica.ui.adapter.tasks.*
import com.habitrpg.android.habitica.ui.fragments.BaseFragment
import com.habitrpg.android.habitica.ui.helpers.SafeDefaultItemAnimator
import com.habitrpg.android.habitica.ui.viewHolders.tasks.BaseTaskViewHolder
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper
import com.habitrpg.android.habitica.ui.views.HabiticaSnackbar
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaAlertDialog
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

open class TaskRecyclerViewFragment : BaseFragment<FragmentRefreshRecyclerviewBinding>(), androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener {
    internal var canEditTasks: Boolean = true
    internal var canScoreTaks: Boolean = true
    override var binding: FragmentRefreshRecyclerviewBinding? = null

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentRefreshRecyclerviewBinding {
        return FragmentRefreshRecyclerviewBinding.inflate(inflater, container, false)
    }

    protected var recyclerSubscription: CompositeDisposable = CompositeDisposable()
    var recyclerAdapter: TaskRecyclerViewAdapter? = null
    var itemAnimator = SafeDefaultItemAnimator()
    var ownerID: String = ""
    @Inject
    lateinit var apiClient: ApiClient
    @Inject
    lateinit var taskFilterHelper: TaskFilterHelper
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

    internal var layoutManager: RecyclerView.LayoutManager? = null

    internal var taskType: String = Task.TYPE_HABIT
    private var itemTouchCallback: ItemTouchHelper.Callback? = null

    var refreshAction: ((() -> Unit) -> Unit)? = null

    internal val className: String
        get() = this.taskType

    // TODO needs a bit of cleanup
    private fun setInnerAdapter() {
        if (binding?.recyclerView?.adapter != null && binding?.recyclerView?.adapter == recyclerAdapter && !recyclerSubscription.isDisposed) {
            return
        }
        if (!recyclerSubscription.isDisposed) {
            recyclerSubscription.dispose()
        }
        recyclerSubscription = CompositeDisposable()
        val adapter: BaseRecyclerViewAdapter<*, *>? = when (this.taskType) {
            Task.TYPE_HABIT -> HabitsRecyclerViewAdapter(null, true, R.layout.habit_item_card, taskFilterHelper)
            Task.TYPE_DAILY -> DailiesRecyclerViewHolder(null, true, R.layout.daily_item_card, taskFilterHelper)
            Task.TYPE_TODO -> TodosRecyclerViewAdapter(null, true, R.layout.todo_item_card, taskFilterHelper)
            Task.TYPE_REWARD -> RewardsRecyclerViewAdapter(null, R.layout.reward_item_card)
            else -> null
        }

        recyclerAdapter = adapter as? TaskRecyclerViewAdapter
        recyclerAdapter?.canScoreTasks = canScoreTaks
        binding?.recyclerView?.adapter = adapter

        context?.let { recyclerAdapter?.taskDisplayMode = configManager.taskDisplayMode(it) }

        recyclerAdapter?.errorButtonEvents?.subscribe({
            taskRepository.syncErroredTasks().subscribe({}, RxErrorHandler.handleEmptyError())
        }, RxErrorHandler.handleEmptyError())?.let { recyclerSubscription.add(it) }
        recyclerAdapter?.taskOpenEvents?.subscribeWithErrorHandler {
            openTaskForm(it)
        }?.let { recyclerSubscription.add(it) }
        recyclerAdapter?.taskScoreEvents
                ?.doOnNext { playSound(it.second) }
                ?.subscribeWithErrorHandler { scoreTask(it.first, it.second) }?.let { recyclerSubscription.add(it) }
        recyclerAdapter?.checklistItemScoreEvents
                ?.flatMap { taskRepository.scoreChecklistItem(it.first.id ?: "", it.second.id ?: "")
                }?.subscribeWithErrorHandler {}?.let { recyclerSubscription.add(it) }
        recyclerAdapter?.brokenTaskEvents?.subscribeWithErrorHandler { showBrokenChallengeDialog(it) }?.let { recyclerSubscription.add(it) }

        recyclerSubscription.add(taskRepository.getTasks(this.taskType, ownerID).subscribe({
            this.recyclerAdapter?.updateUnfilteredData(it)
            this.recyclerAdapter?.filter()
        }, RxErrorHandler.handleEmptyError()))
    }

    private fun handleTaskResult(result: TaskScoringResult, value: Int) {
        if (taskType == Task.TYPE_REWARD) {
            (activity as? MainActivity)?.let { activity ->
                HabiticaSnackbar.showSnackbar(activity.snackbarContainer, null, getString(R.string.notification_purchase_reward),
                        BitmapDrawable(resources, HabiticaIconsHelper.imageOfGold()),
                        ContextCompat.getColor(activity, R.color.yellow_10),
                        "-$value",
                        HabiticaSnackbar.SnackbarDisplayType.DROP)
            }
        } else {
            (activity as? MainActivity)?.displayTaskScoringResponse(result)
        }
    }

    private fun playSound(direction: TaskDirection) {
        val soundName = when (taskType) {
            Task.TYPE_HABIT -> if (direction == TaskDirection.UP) SoundManager.SoundPlusHabit else SoundManager.SoundMinusHabit
            Task.TYPE_DAILY -> SoundManager.SoundDaily
            Task.TYPE_TODO -> SoundManager.SoundTodo
            Task.TYPE_REWARD -> SoundManager.SoundReward
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
        inventoryRepository.close()
        super.onDestroy()
    }

    override fun injectFragment(component: UserComponent) {
        component.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        context?.let { binding?.recyclerView?.setBackgroundColor(ContextCompat.getColor(it, R.color.content_background)) }
        savedInstanceState?.let { this.taskType = savedInstanceState.getString(CLASS_TYPE_KEY, "") }

        when (taskType) {
            Task.TYPE_TODO -> taskFilterHelper.setActiveFilter(Task.TYPE_TODO, Task.FILTER_ACTIVE)
            Task.TYPE_DAILY -> {
                val user = (activity as? MainActivity)?.user
                if (user?.isValid == true && user.preferences?.dailyDueDefaultView == true) {
                    taskFilterHelper.setActiveFilter(Task.TYPE_DAILY, Task.FILTER_ACTIVE)
                }
            }
        }

        recyclerAdapter?.filter()

        itemTouchCallback = object : ItemTouchHelper.Callback() {
            override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                super.onSelectedChanged(viewHolder, actionState)
                if (viewHolder == null || viewHolder.adapterPosition == NO_POSITION) return
                val taskViewHolder = viewHolder as? BaseTaskViewHolder
                if (taskViewHolder != null) {
                    taskViewHolder.movingFromPosition = viewHolder.adapterPosition
                }
                binding?.refreshLayout?.isEnabled = false
            }

            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                recyclerAdapter?.notifyItemMoved(viewHolder.adapterPosition, target.adapterPosition)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) { /* no-on */ }

            //defines the enabled move directions in each state (idle, swiping, dragging).
            override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
                return if (recyclerAdapter?.getItemViewType(viewHolder.adapterPosition) ?: 0 == 2) {
                    makeFlag(ItemTouchHelper.ACTION_STATE_IDLE, 0)
                } else {
                    makeFlag(ItemTouchHelper.ACTION_STATE_DRAG,
                            ItemTouchHelper.DOWN or ItemTouchHelper.UP)
                }
            }

            override fun isItemViewSwipeEnabled(): Boolean = false

            override fun isLongPressDragEnabled(): Boolean = true

            override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                super.clearView(recyclerView, viewHolder)
                binding?.refreshLayout?.isEnabled = true

                if (viewHolder.adapterPosition == NO_POSITION) return
                val taskViewHolder = viewHolder as? BaseTaskViewHolder
                val validTaskId = taskViewHolder?.task?.takeIf { it.isValid }?.id
                if (viewHolder.adapterPosition != taskViewHolder?.movingFromPosition) {
                    taskViewHolder?.movingFromPosition = null
                    updateTaskInRepository(validTaskId, viewHolder)
                }
            }

            private fun updateTaskInRepository(validTaskId: String?, viewHolder: RecyclerView.ViewHolder) {
                if (validTaskId != null) {
                    recyclerAdapter?.ignoreUpdates = true
                    compositeSubscription.add(taskRepository.updateTaskPosition(
                            taskType, validTaskId, viewHolder.adapterPosition
                    )
                            .delay(1, TimeUnit.SECONDS)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({
                                recyclerAdapter?.ignoreUpdates = false
                            }, RxErrorHandler.handleEmptyError()))
                }
            }
        }

        binding?.recyclerView?.setScaledPadding(context, 0, 0, 0, 48)

        layoutManager = getLayoutManager(context)
        layoutManager?.isItemPrefetchEnabled = false
        binding?.recyclerView?.layoutManager = layoutManager

        this.setInnerAdapter()

        allowReordering()

        val bottomPadding = ((binding?.recyclerView?.paddingBottom ?: 0) + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60f, resources.displayMetrics)).toInt()
        binding?.recyclerView?.setPadding(0, 0, 0, bottomPadding)
        binding?.recyclerView?.itemAnimator = itemAnimator

        binding?.refreshLayout?.setOnRefreshListener(this)

        binding?.recyclerView?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    binding?.refreshLayout?.isEnabled = (activity as? MainActivity)?.isAppBarExpanded ?: false
                }
            }
        })

        setEmptyLabels()

        if (Task.TYPE_REWARD == className) {
            compositeSubscription.add(taskRepository.getTasks(this.className, ownerID)
                    .subscribe({ recyclerAdapter?.data = it }, RxErrorHandler.handleEmptyError()))
        }
    }

    protected fun showBrokenChallengeDialog(task: Task) {
        context?.let {
            if (!task.isValid) {
                return
            }
            taskRepository.getTasksForChallenge(task.challengeID).firstElement().subscribe({ tasks ->
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
                dialog.addButton(it.getString(R.string.delete_x_tasks, taskCount), false, true) { _, _ ->
                    if (!task.isValid) return@addButton
                    taskRepository.unlinkAllTasks(task.challengeID, "remove-all")
                            .flatMap { userRepository.retrieveUser(true, forced = true) }
                            .subscribe({}, RxErrorHandler.handleEmptyError())
                }
                dialog.setExtraCloseButtonVisibility(View.VISIBLE)
                dialog.show()
            }, RxErrorHandler.handleEmptyError())
        }
    }

    private fun setEmptyLabels() {
        binding?.recyclerView?.setEmptyView(binding?.emptyView)
        context?.let { binding?.emptyIconView?.setColorFilter(ContextCompat.getColor(it, R.color.text_dimmed), android.graphics.PorterDuff.Mode.MULTIPLY) }
        if (taskFilterHelper.howMany(taskType) > 0) {
            when (this.taskType) {
                Task.TYPE_HABIT -> {
                    binding?.emptyIconView?.setImageResource(R.drawable.icon_habits)
                    binding?.emptyViewTitle?.setText(R.string.empty_title_habits_filtered)
                    binding?.emptyViewDescription?.setText(R.string.empty_description_habits_filtered)
                }
                Task.TYPE_DAILY -> {
                    binding?.emptyIconView?.setImageResource(R.drawable.icon_dailies)
                    binding?.emptyViewTitle?.setText(R.string.empty_title_dailies_filtered)
                    binding?.emptyViewDescription?.setText(R.string.empty_description_dailies_filtered)
                }
                Task.TYPE_TODO -> {
                    binding?.emptyIconView?.setImageResource(R.drawable.icon_todos)
                    binding?.emptyViewTitle?.setText(R.string.empty_title_todos_filtered)
                    binding?.emptyViewDescription?.setText(R.string.empty_description_todos_filtered)
                }
                Task.TYPE_REWARD -> {
                    binding?.emptyIconView?.setImageResource(R.drawable.icon_rewards)
                    binding?.emptyViewTitle?.setText(R.string.empty_title_rewards)
                }
            }
        } else {
            when (this.taskType) {
                Task.TYPE_HABIT -> {
                    binding?.emptyIconView?.setImageResource(R.drawable.icon_habits)
                    binding?.emptyViewTitle?.setText(R.string.empty_title_habits)
                    binding?.emptyViewDescription?.setText(R.string.empty_description_habits)
                }
                Task.TYPE_DAILY -> {
                    binding?.emptyIconView?.setImageResource(R.drawable.icon_dailies)
                    binding?.emptyViewTitle?.setText(R.string.empty_title_dailies)
                    binding?.emptyViewDescription?.setText(R.string.empty_description_dailies)
                }
                Task.TYPE_TODO -> {
                    binding?.emptyIconView?.setImageResource(R.drawable.icon_todos)
                    binding?.emptyViewTitle?.setText(R.string.empty_title_todos)
                    binding?.emptyViewDescription?.setText(R.string.empty_description_todos)
                }
                Task.TYPE_REWARD -> {
                    binding?.emptyIconView?.setImageResource(R.drawable.icon_rewards)
                    binding?.emptyViewTitle?.setText(R.string.empty_title_rewards)
                }
            }
        }
    }

    private fun scoreTask(task: Task, direction: TaskDirection) {
        compositeSubscription.add(taskRepository.taskChecked(null, task, direction == TaskDirection.UP, false) { result ->
            handleTaskResult(result, task.value.toInt())
        }.subscribeWithErrorHandler {})
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(CLASS_TYPE_KEY, this.taskType)
    }

    override val displayedClassName: String?
        get() = this.taskType + super.displayedClassName

    override fun onRefresh() {
        binding?.refreshLayout?.isRefreshing = true
        refreshAction?.invoke {
            binding?.refreshLayout?.isRefreshing = false
        }
    }

    override fun onStart() {
        super.onStart()
        if (binding?.recyclerView?.adapter == null || recyclerAdapter == null) {
            setInnerAdapter()
        }
    }

    override fun onResume() {
        super.onResume()
        context?.let { recyclerAdapter?.taskDisplayMode = configManager.taskDisplayMode(it) }
        setInnerAdapter()
        recyclerAdapter?.filter()
    }

    fun setActiveFilter(activeFilter: String) {
        taskFilterHelper.setActiveFilter(taskType, activeFilter)
        recyclerAdapter?.filter()

        setEmptyLabels()

        if (activeFilter == Task.FILTER_COMPLETED) {
            compositeSubscription.add(taskRepository.retrieveCompletedTodos(ownerID).subscribe({}, RxErrorHandler.handleEmptyError()))
        }
    }

    private fun openTaskForm(task: Task) {
        if (Date().time - (TasksFragment.lastTaskFormOpen?.time ?: 0) < 2000 || !task.isValid || !canEditTasks) {
            return
        }

        val bundle = Bundle()
        bundle.putString(TaskFormActivity.TASK_TYPE_KEY, task.type)
        bundle.putString(TaskFormActivity.TASK_ID_KEY, task.id)
        bundle.putDouble(TaskFormActivity.TASK_VALUE_KEY, task.value)

        val intent = Intent(activity, TaskFormActivity::class.java)
        intent.putExtras(bundle)
        TasksFragment.lastTaskFormOpen = Date()
        if (isAdded) {
            startActivityForResult(intent, TasksFragment.TASK_UPDATED_RESULT)
        }
    }

    companion object {
        private const val CLASS_TYPE_KEY = "CLASS_TYPE_KEY"

        fun newInstance(context: Context?, classType: String): TaskRecyclerViewFragment {
            val fragment = TaskRecyclerViewFragment()
            fragment.retainInstance = true
            fragment.taskType = classType
            var tutorialTexts: List<String>? = null
            if (context != null) {
                when (fragment.taskType) {
                    Task.TYPE_HABIT -> {
                        fragment.tutorialStepIdentifier = "habits"
                        tutorialTexts = listOf(context.getString(R.string.tutorial_overview), context.getString(R.string.tutorial_habits_1), context.getString(R.string.tutorial_habits_2), context.getString(R.string.tutorial_habits_3), context.getString(R.string.tutorial_habits_4))
                    }
                    Task.FREQUENCY_DAILY -> {
                        fragment.tutorialStepIdentifier = "dailies"
                        tutorialTexts = listOf(context.getString(R.string.tutorial_dailies_1), context.getString(R.string.tutorial_dailies_2))
                    }
                    Task.TYPE_TODO -> {
                        fragment.tutorialStepIdentifier = "todos"
                        tutorialTexts = listOf(context.getString(R.string.tutorial_todos_1), context.getString(R.string.tutorial_todos_2))
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
