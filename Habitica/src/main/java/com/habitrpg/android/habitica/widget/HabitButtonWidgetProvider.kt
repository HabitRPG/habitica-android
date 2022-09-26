package com.habitrpg.android.habitica.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.habitrpg.android.habitica.HabiticaBaseApplication
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.TaskRepository
import com.habitrpg.android.habitica.helpers.ExceptionHandler
import com.habitrpg.shared.habitica.models.responses.TaskDirection
import javax.inject.Inject

class HabitButtonWidgetProvider : BaseWidgetProvider() {
    @Inject
    lateinit var taskRepository: TaskRepository

    private fun setUp() {
        if (!hasInjected) {
            hasInjected = true
            HabiticaBaseApplication.userComponent?.inject(this)
        }
    }

    override fun layoutResourceId(): Int {
        return R.layout.widget_habit_button
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        setUp()
        val thisWidget = ComponentName(
            context,
            HabitButtonWidgetProvider::class.java
        )
        val allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)

        for (widgetId in allWidgetIds) {
            val options = appWidgetManager.getAppWidgetOptions(widgetId)
            appWidgetManager.partiallyUpdateAppWidget(
                widgetId,
                sizeRemoteViews(context, options, widgetId)
            )
        }

        // Build the intent to call the service
        val intent = Intent(context.applicationContext, HabitButtonWidgetService::class.java)
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds)

        try {
            context.startService(intent)
        } catch (ignore: IllegalStateException) {
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        setUp()
        if (intent.action == HABIT_ACTION) {
            val mgr = AppWidgetManager.getInstance(context)
            val appWidgetId = intent.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            )
            val taskId = intent.getStringExtra(TASK_ID)
            val direction = intent.getStringExtra(TASK_DIRECTION)

            val ids = intArrayOf(appWidgetId)

            if (taskId != null) {
                userRepository.getUserFlowable().firstElement().flatMap { user -> taskRepository.taskChecked(user, taskId, TaskDirection.UP.text == direction, false, null) }
                    .subscribe({ taskDirectionData -> showToastForTaskDirection(context, taskDirectionData) }, ExceptionHandler.rx(), { this.onUpdate(context, mgr, ids) })
            }
        }
        super.onReceive(context, intent)
    }

    override fun configureRemoteViews(
        remoteViews: RemoteViews,
        widgetId: Int,
        columns: Int,
        rows: Int
    ): RemoteViews {
        return remoteViews
    }

    companion object {

        const val HABIT_ACTION = "com.habitrpg.android.habitica.HABIT_ACTION"
        const val TASK_ID = "com.habitrpg.android.habitica.TASK_ID_ITEM"
        const val TASK_DIRECTION = "com.habitrpg.android.habitica.TASK_DIRECTION"
    }
}
