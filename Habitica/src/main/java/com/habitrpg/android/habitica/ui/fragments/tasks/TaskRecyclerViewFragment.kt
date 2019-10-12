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
import com.habitrpg.android.habitica.extensions.setScaledPadding
import com.habitrpg.android.habitica.extensions.subscribeWithErrorHandler
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.helpers.SoundManager
import com.habitrpg.android.habitica.helpers.TaskFilterHelper
import com.habitrpg.android.habitica.models.responses.TaskDirection
import com.habitrpg.android.habitica.models.responses.TaskScoringResult
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.modules.AppModule
import com.habitrpg.android.habitica.ui.activities.MainActivity
import com.habitrpg.android.habitica.ui.activities.TaskFormActivity
import com.habitrpg.android.habitica.ui.adapter.tasks.*
import com.habitrpg.android.habitica.ui.fragments.BaseFragment
import com.habitrpg.android.habitica.ui.helpers.SafeDefaultItemAnimator
import com.habitrpg.android.habitica.ui.viewHolders.tasks.BaseTaskViewHolder
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper
import com.habitrpg.android.habitica.ui.views.HabiticaSnackbar
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import kotlinx.android.synthetic.main.fragment_refresh_recyclerview.*
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Named

open class TaskRecyclerViewFragment : BaseFragment(), androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener {
    var recyclerAdapter: TaskRecyclerViewAdapter? = null
    var itemAnimator = SafeDefaultItemAnimator()
    @field:[Inject Named(AppModule.NAMED_USER_ID)]
    lateinit var userID: String
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

    internal var classType: String? = null
    internal var user: User? = null
    private var itemTouchCallback: ItemTouchHelper.Callback? = null

    internal val className: String
        get() = this.classType ?: ""

    // TODO needs a bit of cleanup
    private fun setInnerAdapter() {
        val adapter: RecyclerView.Adapter<*>? = when (this.classType) {
            Task.TYPE_HABIT -> {
                HabitsRecyclerViewAdapter(null, true, R.layout.habit_item_card, taskFilterHelper)
            }
            Task.TYPE_DAILY -> {
                DailiesRecyclerViewHolder(null, true, R.layout.daily_item_card, taskFilterHelper)
            }
            Task.TYPE_TODO -> {
                TodosRecyclerViewAdapter(null, true, R.layout.todo_item_card, taskFilterHelper)
            }
            Task.TYPE_REWARD -> {
                RewardsRecyclerViewAdapter(null, R.layout.reward_item_card, user, configManager)
            }
            else -> null
        }

        recyclerAdapter = adapter as? TaskRecyclerViewAdapter
        recyclerView.adapter = adapter

        if (this.classType != null) {
            compositeSubscription.add(taskRepository.getTasks(this.classType ?: "", userID).firstElement().subscribe(Consumer {
                this.recyclerAdapter?.updateUnfilteredData(it)
                this.recyclerAdapter?.filter()
            }, RxErrorHandler.handleEmptyError()))
        }
    }

    private fun handleTaskResult(result: TaskScoringResult, value: Int) {
        if (classType == Task.TYPE_REWARD) {
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
        val soundName = when (classType) {
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
        itemTouchHelper?.attachToRecyclerView(recyclerView)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        if (Task.TYPE_DAILY == classType) {
            if (user?.isValid == true && user?.preferences?.dailyDueDefaultView == true) {
                taskFilterHelper.setActiveFilter(Task.TYPE_DAILY, Task.FILTER_ACTIVE)
            }
        } else if (Task.TYPE_TODO == classType) {
            taskFilterHelper.setActiveFilter(Task.TYPE_TODO, Task.FILTER_ACTIVE)
        }

        itemTouchCallback = object : ItemTouchHelper.Callback() {

            override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                super.onSelectedChanged(viewHolder, actionState)
                if (viewHolder == null || viewHolder.adapterPosition == NO_POSITION) return
                val taskViewHolder = viewHolder as? BaseTaskViewHolder
                if (taskViewHolder != null) {
                    taskViewHolder.movingFromPosition = viewHolder.adapterPosition
                }
                refreshLayout.isEnabled = false
            }

            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                recyclerAdapter?.notifyItemMoved(viewHolder.adapterPosition, target.adapterPosition)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) { /* no-on */ }

            //defines the enabled move directions in each state (idle, swiping, dragging).
            override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
                return makeFlag(ItemTouchHelper.ACTION_STATE_DRAG,
                        ItemTouchHelper.DOWN or ItemTouchHelper.UP)
            }

            override fun isItemViewSwipeEnabled(): Boolean = false

            override fun isLongPressDragEnabled(): Boolean = true

            override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                super.clearView(recyclerView, viewHolder)
                refreshLayout?.isEnabled = true

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
                            classType ?: "", validTaskId, viewHolder.adapterPosition
                    )
                            .delay(1, TimeUnit.SECONDS)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(Consumer {
                                recyclerAdapter?.ignoreUpdates = false
                            }, RxErrorHandler.handleEmptyError()))
                }
            }
        }
        if (savedInstanceState != null) {
            this.classType = savedInstanceState.getString(CLASS_TYPE_KEY, "")
        }

        return inflater.inflate(R.layout.fragment_refresh_recyclerview, container, false)
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
        recyclerView.setScaledPadding(context, 0, 0, 0, 48)
        recyclerView.adapter = recyclerAdapter as? RecyclerView.Adapter<*>
        recyclerAdapter?.filter()

        layoutManager = getLayoutManager(context)
        layoutManager?.isItemPrefetchEnabled = false
        recyclerView.layoutManager = layoutManager

        if (recyclerView.adapter == null) {
            this.setInnerAdapter()
        }

        if (classType != Task.TYPE_REWARD) {
            allowReordering()
        }

        if (this.classType != null) {
            recyclerAdapter?.errorButtonEvents?.subscribe(Consumer {
                taskRepository.syncErroredTasks().subscribe(Consumer {}, RxErrorHandler.handleEmptyError())
            }, RxErrorHandler.handleEmptyError())?.let { compositeSubscription.add(it) }
            recyclerAdapter?.taskOpenEvents?.subscribeWithErrorHandler(Consumer {
                openTaskForm(it)
            })?.let { compositeSubscription.add(it) }
            recyclerAdapter?.taskScoreEvents
                    ?.doOnNext { playSound(it.second) }
                    ?.subscribeWithErrorHandler(Consumer { scoreTask(it.first, it.second) })?.let { compositeSubscription.add(it) }
            recyclerAdapter?.checklistItemScoreEvents
                    ?.flatMap { taskRepository.scoreChecklistItem(it.first.id ?: "", it.second.id ?: "")
                    }?.subscribeWithErrorHandler(Consumer {})?.let { compositeSubscription.add(it) }
        }

        val bottomPadding = (recyclerView.paddingBottom + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60f, resources.displayMetrics)).toInt()
        recyclerView.setPadding(0, 0, 0, bottomPadding)
        recyclerView.itemAnimator = itemAnimator

        refreshLayout.setOnRefreshListener(this)

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    refreshLayout?.isEnabled = (activity as? MainActivity)?.isAppBarExpanded ?: false
                }
            }
        })

        if (this.classType != null) {
            when (this.classType) {
                Task.TYPE_HABIT -> {
                    this.emptyViewTitle.setText(R.string.empty_title_habits)
                    this.emptyViewDescription.setText(R.string.empty_description_habits)
                }
                Task.TYPE_DAILY -> {
                    this.emptyViewTitle.setText(R.string.empty_title_dailies)
                    this.emptyViewDescription.setText(R.string.empty_description_dailies)
                }
                Task.TYPE_TODO -> {
                    this.emptyViewTitle.setText(R.string.empty_title_todos)
                    this.emptyViewDescription.setText(R.string.empty_description_todos)
                }
                Task.TYPE_REWARD -> {
                    this.emptyViewTitle.setText(R.string.empty_title_rewards)
                }
            }
        }

        if (Task.TYPE_REWARD == className) {
            compositeSubscription.add(taskRepository.getTasks(this.className, userID)
                    .subscribe(Consumer { recyclerAdapter?.updateData(it) }, RxErrorHandler.handleEmptyError()))
        }
    }

    private fun scoreTask(task: Task, direction: TaskDirection) {
        compositeSubscription.add(taskRepository.taskChecked(user, task, direction == TaskDirection.UP, false) { result ->
            handleTaskResult(result, task.value.toInt())
        }.subscribeWithErrorHandler(Consumer {}))
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(CLASS_TYPE_KEY, this.classType)
    }

    override val displayedClassName: String?
        get() = this.classType + super.displayedClassName

    override fun onRefresh() {
        refreshLayout.isRefreshing = true
        compositeSubscription.add(userRepository.retrieveUser(true, true)
                .doOnTerminate {
                    refreshLayout?.isRefreshing = false
                }.subscribe(Consumer { }, RxErrorHandler.handleEmptyError()))
    }

    fun setActiveFilter(activeFilter: String) {
        taskFilterHelper.setActiveFilter(classType ?: "", activeFilter)
        recyclerAdapter?.filter()

        if (activeFilter == Task.FILTER_COMPLETED) {
            compositeSubscription.add(taskRepository.retrieveCompletedTodos(userID).subscribe(Consumer {}, RxErrorHandler.handleEmptyError()))
        }
    }

    private fun openTaskForm(task: Task) {
        if (Date().time - (TasksFragment.lastTaskFormOpen?.time ?: 0) < 2000 || !task.isValid) {
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

        fun newInstance(context: Context?, user: User?, classType: String): TaskRecyclerViewFragment {
            val fragment = TaskRecyclerViewFragment()
            fragment.retainInstance = true
            fragment.user = user
            fragment.classType = classType
            var tutorialTexts: List<String>? = null
            if (context != null) {
                when (fragment.classType) {
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
