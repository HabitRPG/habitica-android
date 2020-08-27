package com.habitrpg.android.habitica.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.text.SpannableStringBuilder
import android.text.format.DateUtils
import android.text.style.DynamicDrawableSpan
import android.util.Log
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.habitrpg.android.habitica.HabiticaBaseApplication
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.TaskRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.ui.helpers.MarkdownParser
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import net.pherth.android.emoji_library.EmojiHandler
import java.util.*
import javax.inject.Inject

abstract class TaskListFactory internal constructor(val context: Context, intent: Intent, private val taskType: String, private val listItemResId: Int, private val listItemTextResId: Int) : RemoteViewsService.RemoteViewsFactory {
    private val widgetId: Int = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 0)
    @Inject
    lateinit var taskRepository: TaskRepository
    @Inject
    lateinit var userRepository: UserRepository
    private var taskList: List<Task> = ArrayList()
    private var reloadData: Boolean = false

    init {
        this.reloadData = false
    }

    private fun isTodayOrNull(date: Date?): Boolean {

        if (date == null || DateUtils.isToday(date.time)){
            return true;
        }

        return false;
    }


    private fun loadData() {
        if (!this::taskRepository.isInitialized) {
            return
        }
        val mainHandler = Handler(context.mainLooper)
        mainHandler.post {
            taskRepository.getCurrentUserTasks(taskType)
                    .firstElement()
                    .toObservable()
                    .flatMap { Observable.fromIterable(it) }
                    .filter { task -> task.type == Task.TYPE_TODO && isTodayOrNull(task.dueDate) && !task.completed || task.isDisplayedActive }
                    .toList()
                    .flatMapMaybe { tasks -> taskRepository.getTaskCopies(tasks).firstElement() }
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(Consumer { tasks ->
                        reloadData = false
                        taskList = tasks
                        AppWidgetManager.getInstance(context).notifyAppWidgetViewDataChanged(widgetId, R.id.list_view)
                    }, RxErrorHandler.handleEmptyError())
        }

    }

    override fun onCreate() {
        HabiticaBaseApplication.userComponent?.inject(this)
        this.loadData()
    }
    override fun onDestroy() { /* no-op */ }

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
            EmojiHandler.addEmojis(this.context, builder, 16, DynamicDrawableSpan.ALIGN_BASELINE, 16, 0, -1, false)

            remoteView.setTextViewText(listItemTextResId, builder)
            remoteView.setInt(R.id.checkbox_background, "setBackgroundResource", task.lightTaskColor)
            val fillInIntent = Intent()
            fillInIntent.putExtra(TaskListWidgetProvider.TASK_ID_ITEM, task.id)
            remoteView.setOnClickFillInIntent(R.id.widget_list_row, fillInIntent)
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
