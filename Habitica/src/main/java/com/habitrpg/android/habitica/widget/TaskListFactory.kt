package com.habitrpg.android.habitica.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.text.SpannableStringBuilder
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.TaskRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.common.habitica.helpers.ExceptionHandler
import com.habitrpg.common.habitica.helpers.MarkdownParser
import com.habitrpg.shared.habitica.models.tasks.TaskType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

abstract class TaskListFactory internal constructor(
    val context: Context,
    intent: Intent,
    private val taskType: TaskType,
    private val listItemResId: Int,
    private val listItemTextResId: Int,
    val taskRepository: TaskRepository,
    val userRepository: UserRepository
) : RemoteViewsService.RemoteViewsFactory {
    private val job = SupervisorJob()

    private val widgetId: Int = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 0)
    private var taskList: List<Task> = ArrayList()
    private var reloadData: Boolean = false

    init {
        this.reloadData = false
    }

    private fun loadData() {
        CoroutineScope(Dispatchers.Main + job).launch(ExceptionHandler.coroutine()) {
            val mirroredTasks =
                userRepository.getUser()
                    .firstOrNull()?.preferences?.tasks?.mirrorGroupTasks?.toTypedArray()
            val tasks =
                taskRepository.getTasks(taskType, null, mirroredTasks ?: emptyArray()).firstOrNull()
                    ?.filter { task ->
                        task.type == TaskType.TODO && !task.completed || task.isDisplayedActive
                    } ?: return@launch
            taskList = taskRepository.getTaskCopies(tasks)
            reloadData = false
            AppWidgetManager.getInstance(context)
                .notifyAppWidgetViewDataChanged(widgetId, R.id.list_view)
        }
    }

    override fun onCreate() {
        this.loadData()
    }

    override fun onDestroy() { // no-op
    }

    override fun onDataSetChanged() {
        if (this.reloadData) {
            this.loadData()
        }
        this.reloadData = true
    }

    override fun getCount(): Int {
        return taskList.size
    }

    override fun getViewAt(position: Int): RemoteViews {
        val remoteView = RemoteViews(context.packageName, listItemResId)
        if (taskList.size > position) {
            val task = taskList[position]

            val parsedText = MarkdownParser.parseMarkdown(task.text)

            val builder = SpannableStringBuilder(parsedText)

            remoteView.setTextViewText(listItemTextResId, builder)
            remoteView.setInt(
                R.id.checkbox_background,
                "setBackgroundResource",
                task.lightTaskColor
            )
            val fillInIntent = Intent()
            fillInIntent.putExtra(TaskListWidgetProvider.TASK_ID_ITEM, task.id)
            remoteView.setOnClickFillInIntent(R.id.checkbox_background, fillInIntent)
        }
        return remoteView
    }

    override fun getLoadingView(): RemoteViews {
        return RemoteViews(context.packageName, listItemResId)
    }

    override fun getViewTypeCount(): Int {
        return 1
    }

    override fun getItemId(position: Int): Long {
        if (taskList.size > position) {
            val task = taskList[position]
            return task.id.hashCode().toLong()
        }
        return position.toLong()
    }

    override fun hasStableIds(): Boolean {
        return true
    }
}
