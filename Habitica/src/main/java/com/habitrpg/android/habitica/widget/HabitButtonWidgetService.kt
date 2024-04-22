package com.habitrpg.android.habitica.widget

import android.app.PendingIntent
import android.app.Service
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.IBinder
import android.text.SpannableStringBuilder
import android.view.View
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.TaskRepository
import com.habitrpg.android.habitica.extensions.withImmutableFlag
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.common.habitica.helpers.ExceptionHandler
import com.habitrpg.common.habitica.helpers.MarkdownParser
import com.habitrpg.shared.habitica.models.responses.TaskDirection
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.min

@AndroidEntryPoint
class HabitButtonWidgetService : Service() {
    @Inject
    lateinit var sharedPreferences: SharedPreferences

    @Inject
    @ApplicationContext
    lateinit var context: Context

    @Inject
    lateinit var taskRepository: TaskRepository
    private var appWidgetManager: AppWidgetManager? = null

    private var taskMapping = mutableMapOf<String, Int>()
    private var allWidgetIds: IntArray? = null

    override fun onStartCommand(
        intent: Intent,
        flags: Int,
        startId: Int,
    ): Int {
        this.appWidgetManager = AppWidgetManager.getInstance(this)
        val thisWidget = ComponentName(this, HabitButtonWidgetProvider::class.java)
        allWidgetIds = appWidgetManager?.getAppWidgetIds(thisWidget)
        makeTaskMapping()

        MainScope().launch(ExceptionHandler.coroutine()) {
            for (taskid in taskMapping.keys) {
                val task = taskRepository.getUnmanagedTask(taskid).firstOrNull()
                updateData(task)
            }
        }

        stopSelf()

        return START_STICKY
    }

    private fun updateData(task: Task?) {
        val remoteViews = RemoteViews(this.packageName, R.layout.widget_habit_button)
        if (task != null && task.isValid) {
            val parsedText = MarkdownParser.parseMarkdown(task.text)

            val builder = SpannableStringBuilder(parsedText)
            remoteViews.setTextViewText(
                R.id.habit_title,
                builder.substring(0, min(builder.length, 70)),
            )

            if (task.up != true) {
                remoteViews.setViewVisibility(R.id.btnPlusWrapper, View.GONE)
                remoteViews.setOnClickPendingIntent(R.id.btnPlusWrapper, null)
            } else {
                remoteViews.setViewVisibility(R.id.btnPlusWrapper, View.VISIBLE)
                remoteViews.setInt(
                    R.id.btnPlus,
                    "setBackgroundColor",
                    ContextCompat.getColor(context, task.lightTaskColor),
                )
                remoteViews.setOnClickPendingIntent(
                    R.id.btnPlusWrapper,
                    getPendingIntent(task.id, TaskDirection.UP.text, taskMapping[task.id]!!),
                )
            }
            if (task.down != true) {
                remoteViews.setViewVisibility(R.id.btnMinusWrapper, View.GONE)
                remoteViews.setOnClickPendingIntent(R.id.btnMinusWrapper, null)
            } else {
                remoteViews.setViewVisibility(R.id.btnMinusWrapper, View.VISIBLE)
                remoteViews.setInt(
                    R.id.btnMinus,
                    "setBackgroundColor",
                    ContextCompat.getColor(context, task.mediumTaskColor),
                )
                remoteViews.setOnClickPendingIntent(
                    R.id.btnMinusWrapper,
                    getPendingIntent(task.id, TaskDirection.DOWN.text, taskMapping[task.id]!!),
                )
            }
            if (taskMapping[task.id] != null) {
                appWidgetManager?.updateAppWidget(taskMapping[task.id]!!, remoteViews)
            }
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun makeTaskMapping() {
        this.taskMapping = HashMap()
        for (widgetId in allWidgetIds!!) {
            val taskId = getTaskId(widgetId)
            if (taskId != "") {
                this.taskMapping[taskId] = widgetId
            }
        }
    }

    private fun getTaskId(widgetId: Int): String {
        return sharedPreferences.getString("habit_button_widget_$widgetId", "") ?: ""
    }

    private fun getPendingIntent(
        taskId: String?,
        direction: String,
        widgetId: Int,
    ): PendingIntent {
        val taskIntent = Intent(context, HabitButtonWidgetProvider::class.java)
        taskIntent.action = HabitButtonWidgetProvider.HABIT_ACTION
        taskIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
        taskIntent.putExtra(HabitButtonWidgetProvider.TASK_ID, taskId)
        taskIntent.putExtra(HabitButtonWidgetProvider.TASK_DIRECTION, direction)
        return PendingIntent.getBroadcast(
            context,
            widgetId + direction.hashCode(),
            taskIntent,
            withImmutableFlag(PendingIntent.FLAG_UPDATE_CURRENT),
        )
    }
}
