package com.habitrpg.android.habitica.ui.views.yesterdailies

import android.app.Activity
import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.TaskRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.extensions.dpToPx
import com.habitrpg.android.habitica.helpers.AmplitudeManager
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.tasks.ChecklistItem
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.ui.helpers.bindColor
import com.habitrpg.android.habitica.ui.views.HabiticaEmojiTextView
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaAlertDialog
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import java.util.*
import java.util.concurrent.TimeUnit

class YesterdailyDialog private constructor(context: Context, private val userRepository: UserRepository, private val taskRepository: TaskRepository, private val tasks: List<Task>) : HabiticaAlertDialog(context) {

    private lateinit var yesterdailiesList: LinearLayout

    private val taskGray: Int by bindColor(context, R.color.task_gray)

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

        //Can't use by bindView() because the view doesn't seem to be available through that yet
        val listView = view?.findViewById(R.id.yesterdailies_list) as? LinearLayout
        if (listView != null) {
            yesterdailiesList = listView
        }
        if (inflater != null) {
            createTaskViews(inflater)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isDisplaying = true
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
        isDisplaying = false
    }

    private fun createTaskViews(inflater: LayoutInflater) {
        for (task in tasks) {
            val taskView = createNewTaskView(inflater)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                taskView.clipToOutline = true
            }
            configureTaskView(taskView, task)
            val taskContainer = taskView.findViewById<View>(R.id.taskHolder)
            taskContainer.setOnClickListener {
                task.completed = !task.completed
                configureTaskView(taskView, task)
            }

            if (task.checklist?.size ?: 0 > 0) {
                val checklistDivider = taskView.findViewById<View>(R.id.checklistDivider)
                checklistDivider.visibility = View.VISIBLE
                val checklistContainer = taskView.findViewById<ViewGroup>(R.id.checklistView)
                for (item in task.checklist ?: emptyList<ChecklistItem>()) {
                    val checklistView = inflater.inflate(R.layout.checklist_item_row, yesterdailiesList, false)
                    configureChecklistView(checklistView, item)
                    checklistView.setOnClickListener {
                        item.completed = !item.completed
                        taskRepository.scoreChecklistItem(task.id ?: "", item.id ?: "").subscribe(Consumer { }, RxErrorHandler.handleEmptyError())
                        configureChecklistView(checklistView, item)
                    }
                    checklistContainer.addView(checklistView)
                }
            }
            val checkBox = taskView.findViewById(R.id.checkBox) as? CheckBox
            checkBox?.isEnabled = false
            checkBox?.isClickable = false
            yesterdailiesList.addView(taskView)
        }
    }

    private fun configureChecklistView(checklistView: View, item: ChecklistItem) {
        val checkbox = checklistView.findViewById(R.id.checkBox) as? CheckBox
        checkbox?.isChecked = item.completed
        val checkboxHolder = checklistView.findViewById<View>(R.id.checkBoxHolder)
        checkboxHolder.setBackgroundResource(R.color.gray_700)
        val textView = checklistView.findViewById(R.id.checkedTextView) as? TextView
        textView?.text = item.text
    }

    private fun configureTaskView(taskView: View, task: Task) {
        val completed = !task.isDisplayedActive
        val checkbox = taskView.findViewById(R.id.checkBox) as? CheckBox
        val checkboxHolder = taskView.findViewById<View>(R.id.checkBoxHolder)
        checkbox?.isChecked = completed
        if (completed) {
            checkboxHolder.setBackgroundColor(this.taskGray)
        } else {
            checkboxHolder.setBackgroundResource(task.lightTaskColor)
        }

        val emojiView = taskView.findViewById<View>(R.id.text_view) as? HabiticaEmojiTextView
        emojiView?.text = task.markdownText { emojiView?.text = it }

    }


    private fun createNewTaskView(inflater: LayoutInflater): View {
        return inflater.inflate(R.layout.dialog_yesterdaily_task, yesterdailiesList, false)
    }

    companion object {

        internal var isDisplaying = false
        internal var lastCronRun: Date? = null

        fun showDialogIfNeeded(activity: Activity, userId: String?, userRepository: UserRepository?, taskRepository: TaskRepository) {
            if (userRepository != null && userId != null) {
                Observable.just("")
                        .delay(500, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                        .filter { !userRepository.isClosed }
                        .flatMapMaybe { userRepository.getUser(userId).firstElement() }
                        .filter { user -> user.needsCron }
                        .flatMapMaybe {
                            val cal = Calendar.getInstance()
                            cal.add(Calendar.DATE, -1)
                            taskRepository.updateDailiesIsDue(cal.time).firstElement()
                        }
                        .flatMapMaybe { taskRepository.getTasks(Task.TYPE_DAILY, userId).firstElement() }
                        .map { tasks -> tasks.where().equalTo("isDue", true).notEqualTo("completed", true).notEqualTo("yesterDaily", false).findAll() }
                        .flatMapMaybe<List<Task>> { tasks -> taskRepository.getTaskCopies(tasks).firstElement() }
                        .retry(1)
                        .throttleFirst(2, TimeUnit.SECONDS)
                        .subscribe(Consumer { tasks ->
                            if (isDisplaying) {
                                return@Consumer
                            }

                            if (Math.abs((lastCronRun?.time ?: 0) - Date().time) < 60 * 60 * 1000L) {
                                return@Consumer
                            }
                            val additionalData = HashMap<String, Any>()
                            additionalData["task count"] = tasks.size
                            AmplitudeManager.sendEvent("show cron", AmplitudeManager.EVENT_CATEGORY_BEHAVIOUR, AmplitudeManager.EVENT_HITTYPE_EVENT, additionalData)

                            if (tasks.isNotEmpty()) {
                                isDisplaying = true
                                showDialog(activity, userRepository, taskRepository, tasks)
                            } else {
                                lastCronRun = Date()
                                userRepository.runCron()
                            }
                        }, RxErrorHandler.handleEmptyError())
            }
        }

        private fun showDialog(activity: Activity, userRepository: UserRepository, taskRepository: TaskRepository, tasks: List<Task>) {
            val dialog = YesterdailyDialog(activity, userRepository, taskRepository, tasks)
            dialog.setCancelable(false)
            dialog.setCanceledOnTouchOutside(false)
            if (!activity.isFinishing) {
                dialog.show()
            }
        }
    }
}
