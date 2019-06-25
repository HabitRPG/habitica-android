package com.habitrpg.android.habitica.ui.fragments.tasks

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.data.TaskRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.extensions.setScaledPadding
import com.habitrpg.android.habitica.extensions.subscribeWithErrorHandler
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

    internal var layoutManager: androidx.recyclerview.widget.RecyclerView.LayoutManager? = null

    internal var classType: String? = null
    internal var user: User? = null
    private var mItemTouchCallback: ItemTouchHelper.Callback? = null

    internal val className: String
        get() = this.classType ?: ""

    // TODO needs a bit of cleanup
    private fun setInnerAdapter() {
        val adapter: androidx.recyclerview.widget.RecyclerView.Adapter<*>? = when (this.classType) {
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
                RewardsRecyclerViewAdapter(null, R.layout.reward_item_card, user)
            }
            else -> null
        }

        if (classType != Task.TYPE_REWARD) {
            allowReordering()
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
        val itemTouchHelper = mItemTouchCallback?.let { ItemTouchHelper(it) }
        itemTouchHelper?.attachToRecyclerView(recyclerView)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        if (Task.TYPE_DAILY == classType) {
            if (user != null && user?.preferences?.dailyDueDefaultView == true) {
                taskFilterHelper.setActiveFilter(Task.TYPE_DAILY, Task.FILTER_ACTIVE)
            }
        } else if (Task.TYPE_TODO == classType) {
            taskFilterHelper.setActiveFilter(Task.TYPE_TODO, Task.FILTER_ACTIVE)
        }

        mItemTouchCallback = object : ItemTouchHelper.Callback() {
            private var fromPosition: Int? = null
            private var movingTaskID: String? = null

            override fun onSelectedChanged(viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder?, actionState: Int) {
                super.onSelectedChanged(viewHolder, actionState)
                if (viewHolder != null) {
                    viewHolder.itemView.setBackgroundColor(Color.LTGRAY)
                    if (fromPosition == null) {
                        fromPosition = viewHolder.adapterPosition
                    }
                    if (movingTaskID == null && (viewHolder as? BaseTaskViewHolder)?.task?.isValid == true) {
                        movingTaskID = (viewHolder as? BaseTaskViewHolder)?.task?.id
                    }
                }
                refreshLayout.isEnabled = false
            }

            override fun onMove(recyclerView: androidx.recyclerview.widget.RecyclerView, viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder, target: androidx.recyclerview.widget.RecyclerView.ViewHolder): Boolean {
                recyclerAdapter?.notifyItemMoved(viewHolder.adapterPosition, target.adapterPosition)
                //taskRepository.swapTaskPosition(viewHolder.getAdapterPosition(), target.getAdapterPosition());
                return true
            }

            override fun onSwiped(viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder, direction: Int) {}

            //defines the enabled move directions in each state (idle, swiping, dragging).
            override fun getMovementFlags(recyclerView: androidx.recyclerview.widget.RecyclerView, viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder): Int {
                return makeFlag(ItemTouchHelper.ACTION_STATE_DRAG,
                        ItemTouchHelper.DOWN or ItemTouchHelper.UP)
            }

            override fun isItemViewSwipeEnabled(): Boolean = false

            override fun isLongPressDragEnabled(): Boolean = true

            override fun clearView(recyclerView: androidx.recyclerview.widget.RecyclerView, viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder) {
                super.clearView(recyclerView, viewHolder)
                refreshLayout?.isEnabled = true

                val fromPosition = fromPosition
                val movingTaskID = movingTaskID
                if (fromPosition != null && movingTaskID != null) {
                    recyclerAdapter?.ignoreUpdates = true
                    compositeSubscription.add(taskRepository.updateTaskPosition(classType ?: "", movingTaskID, viewHolder.adapterPosition)
                            .delay(1, TimeUnit.SECONDS)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(Consumer { recyclerAdapter?.ignoreUpdates = false
                            recyclerAdapter?.notifyDataSetChanged()}, RxErrorHandler.handleEmptyError()))
                }
                this.fromPosition = null
                this.movingTaskID = null
            }
        }
        if (savedInstanceState != null) {
            this.classType = savedInstanceState.getString(CLASS_TYPE_KEY, "")
        }

        return inflater.inflate(R.layout.fragment_refresh_recyclerview, container, false)
    }

    protected open fun getLayoutManager(context: Context?): androidx.recyclerview.widget.LinearLayoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)

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
        recyclerView.adapter = recyclerAdapter as? androidx.recyclerview.widget.RecyclerView.Adapter<*>
        recyclerAdapter?.filter()

        layoutManager = recyclerView.layoutManager

        if (layoutManager == null) {
            layoutManager = getLayoutManager(context)

            recyclerView.layoutManager = layoutManager
        }
        if (recyclerView.adapter == null) {
            this.setInnerAdapter()
        }
        if (this.classType != null) {
            recyclerAdapter?.errorButtonEvents
            recyclerAdapter?.errorButtonEvents?.subscribe(Consumer {
                taskRepository.syncErroredTasks().subscribe(Consumer {}, RxErrorHandler.handleEmptyError())
            }, RxErrorHandler.handleEmptyError())?.let { compositeSubscription.add(it) }
            recyclerAdapter?.taskOpenEvents?.subscribeWithErrorHandler(Consumer {
                openTaskForm(it)
            })?.let { compositeSubscription.add(it) }
            recyclerAdapter?.taskScoreEvents
                    ?.doOnNext { playSound(it.second) }
                    ?.flatMap { taskRepository.taskChecked(user, it.first, it.second == TaskDirection.UP, false) { result ->
                        handleTaskResult(result, it.first.value.toInt())
                    }}?.subscribeWithErrorHandler(Consumer {})?.let { compositeSubscription.add(it) }
            recyclerAdapter?.checklistItemScoreEvents
                    ?.flatMap { taskRepository.scoreChecklistItem(it.first.id ?: "", it.second.id ?: "")
                    }?.subscribeWithErrorHandler(Consumer {})?.let { compositeSubscription.add(it) }
        }

        val bottomPadding = (recyclerView.paddingBottom + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60f, resources.displayMetrics)).toInt()
        recyclerView.setPadding(0, 0, 0, bottomPadding)
        recyclerView.itemAnimator = SafeDefaultItemAnimator()

        refreshLayout.setOnRefreshListener(this)

        recyclerView.addOnScrollListener(object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: androidx.recyclerview.widget.RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE) {
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
        if (Date().time - (TasksFragment.lastTaskFormOpen?.time ?: 0) < 2000) {
            return
        }

        val bundle = Bundle()
        bundle.putString(TaskFormActivity.TASK_TYPE_KEY, task.type)
        bundle.putString(TaskFormActivity.TASK_ID_KEY, task.id)

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
                        tutorialTexts = Arrays.asList(context.getString(R.string.tutorial_overview),
                                context.getString(R.string.tutorial_habits_1),
                                context.getString(R.string.tutorial_habits_2),
                                context.getString(R.string.tutorial_habits_3),
                                context.getString(R.string.tutorial_habits_4))
                    }
                    Task.FREQUENCY_DAILY -> {
                        fragment.tutorialStepIdentifier = "dailies"
                        tutorialTexts = Arrays.asList(context.getString(R.string.tutorial_dailies_1),
                                context.getString(R.string.tutorial_dailies_2))
                    }
                    Task.TYPE_TODO -> {
                        fragment.tutorialStepIdentifier = "todos"
                        tutorialTexts = Arrays.asList(context.getString(R.string.tutorial_todos_1),
                                context.getString(R.string.tutorial_todos_2))
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
