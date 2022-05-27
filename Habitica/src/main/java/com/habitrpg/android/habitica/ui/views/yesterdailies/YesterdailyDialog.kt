package com.habitrpg.android.habitica.ui.views.yesterdailies

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.TaskRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.extensions.dpToPx
import com.habitrpg.android.habitica.extensions.isUsingNightModeResources
import com.habitrpg.android.habitica.helpers.AmplitudeManager
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.tasks.ChecklistItem
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.models.tasks.TaskType
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaAlertDialog
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import java.lang.ref.WeakReference
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit
import kotlin.math.abs

class YesterdailyDialog private constructor(
    context: Context,
    private val userRepository: UserRepository,
    private val taskRepository: TaskRepository,
    private val tasks: List<Task>
) : HabiticaAlertDialog(context) {

    private lateinit var yesterdailiesList: LinearLayout
    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as? LayoutInflater
        val view = inflater?.inflate(R.layout.dialog_yesterdaily, null)
        setTitle(R.string.welcome_back)
        setMessage(R.string.yesterdaililes_prompt)
        this.setAdditionalContentView(view)
        setAdditionalContentSidePadding(9.dpToPx(context))

        addButton(R.string.start_day, true)

        this.setOnDismissListener {
            lastCronRun = Date()
            runCron()
        }

        val listView = view?.findViewById(R.id.yesterdailies_list) as? LinearLayout
        if (listView != null) {
            yesterdailiesList = listView
        }
        if (inflater != null) {
            createTaskViews(inflater)
        }

        dialogWidth = 360.dpToPx(context)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        displayedDialog = WeakReference(this)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        displayedDialog = null
    }

    private fun runCron() {
        val completedTasks = ArrayList<Task>()
        for (task in tasks) {
            if (task.completed) {
                completedTasks.add(task)
            }
        }
        lastCronRun = Date()
        userRepository.runCron(completedTasks)
        displayedDialog = null
    }

    private fun createTaskViews(inflater: LayoutInflater) {
        for (task in tasks) {
            val taskView = createNewTaskView(inflater)
            taskView.clipToOutline = true
            configureTaskView(taskView, task)
            val taskContainer = taskView.findViewById<View>(R.id.taskHolder)
            taskContainer.setOnClickListener {
                task.completed = !task.completed
                configureTaskView(taskView, task)

                if (task.checklist?.size ?: 0 > 0) {
                    val checklistContainer = taskView.findViewById<ViewGroup>(R.id.checklistView)
                    checklistContainer.removeAllViews()
                    for (item in task.checklist ?: emptyList<ChecklistItem>()) {
                        val checklistView = inflater.inflate(R.layout.checklist_item_row, checklistContainer, false) as ViewGroup
                        configureChecklistView(checklistView, task, item)
                        checklistContainer.addView(checklistView)
                    }
                }
            }

            if (task.checklist?.size ?: 0 > 0) {
                val checklistContainer = taskView.findViewById<ViewGroup>(R.id.checklistView)
                for (item in task.checklist ?: emptyList<ChecklistItem>()) {
                    val checklistView = inflater.inflate(R.layout.checklist_item_row, checklistContainer, false) as ViewGroup
                    configureChecklistView(checklistView, task, item)
                    checklistContainer.addView(checklistView)
                }
            }
            yesterdailiesList.addView(taskView)
        }
    }

    private fun configureChecklistView(checklistView: ViewGroup, task: Task, item: ChecklistItem) {
        val checkmark = checklistView.findViewById<ImageView>(R.id.checkmark)
        if (task.completed) {
            checkmark?.drawable?.setTint(ContextCompat.getColor(context, R.color.gray_400))
        } else {
            checkmark?.drawable?.setTint(ContextCompat.getColor(context, task.darkestTaskColor))
        }
        checkmark?.visibility = if (item.completed) View.VISIBLE else View.GONE
        val checkboxHolder = checklistView.findViewById<View>(R.id.checkBoxHolder) as? ViewGroup
        checkboxHolder?.setOnClickListener { _ ->
            item.completed = !item.completed
            taskRepository.scoreChecklistItem(task.id ?: "", item.id ?: "").subscribe({ }, RxErrorHandler.handleEmptyError())
            configureChecklistView(checklistView, task, item)
        }
        checklistView.setOnClickListener {
            item.completed = !item.completed
            taskRepository.scoreChecklistItem(task.id ?: "", item.id ?: "").subscribe({ }, RxErrorHandler.handleEmptyError())
            configureChecklistView(checklistView, task, item)
        }
        checkboxHolder?.setBackgroundResource(
            if (task.completed) {
                R.color.offset_background
            } else {
                task.extraLightTaskColor
            }
        )
        val textView = checklistView.findViewById(R.id.checkedTextView) as? TextView
        textView?.text = item.text
        val checkboxBackground = checklistView.findViewById<View>(R.id.checkBoxBackground)
        checkboxBackground?.backgroundTintList = ContextCompat.getColorStateList(
            context,
            (
                if (context.isUsingNightModeResources()) {
                    if (task.completed) {
                        R.color.checkbox_fill
                    } else {
                        task.lightTaskColor
                    }
                } else {
                    R.color.checkbox_fill
                }
                )
        )
    }

    private fun configureTaskView(taskView: View, task: Task) {
        val completed = !task.isDisplayedActive
        val checkmark = taskView.findViewById<ImageView>(R.id.checkmark)
        checkmark?.drawable?.setTint(ContextCompat.getColor(context, R.color.gray_400))
        val checkboxHolder = taskView.findViewById<View>(R.id.checkBoxHolder)
        val checkboxBackground = taskView.findViewById<View>(R.id.checkbox_background)
        checkmark?.visibility = if (completed) View.VISIBLE else View.GONE
        if (completed) {
            checkboxHolder.setBackgroundResource(R.color.window_background)
            checkboxBackground.setBackgroundResource(R.drawable.daily_checked)
        } else {
            checkboxHolder.setBackgroundResource(task.lightTaskColor)
            checkboxBackground.setBackgroundResource(R.drawable.daily_unchecked)
        }

        val emojiView = taskView.findViewById<TextView>(R.id.text_view)
        emojiView?.text = task.markdownText { emojiView?.text = it }
    }

    private fun createNewTaskView(inflater: LayoutInflater): View {
        return inflater.inflate(R.layout.dialog_yesterdaily_task, yesterdailiesList, false)
    }

    companion object {

        private var displayedDialog: WeakReference<YesterdailyDialog>? = null
        internal var lastCronRun: Date? = null

        fun showDialogIfNeeded(
            activity: Activity,
            userId: String?,
            userRepository: UserRepository?,
            taskRepository: TaskRepository
        ) {
            if (userRepository != null && userId != null) {
                Observable.just("")
                    .delay(500, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                    .filter { !userRepository.isClosed }
                    .flatMapMaybe { userRepository.getUserFlowable().firstElement() }
                    .filter { it.needsCron }
                    .flatMapMaybe {
                        val cal = Calendar.getInstance()
                        cal.add(Calendar.DATE, -1)
                        taskRepository.retrieveDailiesFromDate(cal.time).firstElement()
                    }
                    .map {
                        it.tasks.values.filter { task ->
                            return@filter task.type == TaskType.DAILY && task.isDue == true && !task.completed && task.yesterDaily
                        }
                    }
                    .retry(1)
                    .throttleFirst(2, TimeUnit.SECONDS)
                    .filter {
                        if (displayedDialog?.get()?.isShowing == true) {
                            return@filter false
                        }

                        if (abs((lastCronRun?.time ?: 0) - Date().time) < 60 * 60 * 1000L) {
                            return@filter false
                        }
                        return@filter true
                    }
                    .firstElement()
                    .zipWith(
                        taskRepository.getTasks(TaskType.DAILY).firstElement()
                            .map {
                                val taskMap = mutableMapOf<String, Int>()
                                it.forEachIndexed { index, task -> taskMap[task.id ?: ""] = index }
                                taskMap
                            }
                    ) { yesterdayTasks, dailies ->
                        yesterdayTasks.sortedBy { dailies[it.id ?: ""] }
                    }
                    .subscribe(
                        { tasks ->
                            val additionalData = HashMap<String, Any>()
                            additionalData["task count"] = tasks.size
                            AmplitudeManager.sendEvent("show cron", AmplitudeManager.EVENT_CATEGORY_BEHAVIOUR, AmplitudeManager.EVENT_HITTYPE_EVENT, additionalData)

                            if (tasks.isNotEmpty()) {
                                displayedDialog = WeakReference(showDialog(activity, userRepository, taskRepository, tasks))
                            } else {
                                lastCronRun = Date()
                                userRepository.runCron()
                            }
                        },
                        RxErrorHandler.handleEmptyError()
                    )
            }
        }

        private fun showDialog(
            activity: Activity,
            userRepository: UserRepository,
            taskRepository: TaskRepository,
            tasks: List<Task>
        ): YesterdailyDialog {
            val dialog = YesterdailyDialog(activity, userRepository, taskRepository, tasks)
            dialog.setCancelable(false)
            dialog.setCanceledOnTouchOutside(false)
            if (!activity.isFinishing) {
                dialog.enqueue()
            }
            return dialog
        }
    }
}
