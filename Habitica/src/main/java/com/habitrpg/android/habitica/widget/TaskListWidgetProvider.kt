package com.habitrpg.android.habitica.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import com.habitrpg.android.habitica.HabiticaBaseApplication
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.TaskRepository
import com.habitrpg.android.habitica.extensions.withImmutableFlag
import com.habitrpg.android.habitica.extensions.withMutableFlag
import com.habitrpg.common.habitica.helpers.ExceptionHandler
import com.habitrpg.android.habitica.ui.activities.MainActivity
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

abstract class TaskListWidgetProvider : BaseWidgetProvider() {

    @Inject
    lateinit var taskRepository: TaskRepository

    protected abstract val serviceClass: Class<*>

    protected abstract val providerClass: Class<*>

    protected abstract val titleResId: Int

    private fun setUp() {
        if (!hasInjected) {
            hasInjected = true
            HabiticaBaseApplication.userComponent?.inject(this)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        setUp()
        if (intent.action == DAILY_ACTION) {
            val appWidgetId = intent.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            )
            val taskId = intent.getStringExtra(TASK_ID_ITEM)

            if (taskId != null) {
                MainScope().launch(ExceptionHandler.coroutine()) {
                    val user = userRepository.getUser().firstOrNull()
                    val response = taskRepository.taskChecked(user, taskId, up = true, force = false, notifyFunc = null)
                    showToastForTaskDirection(context, response)
                    AppWidgetManager.getInstance(context).notifyAppWidgetViewDataChanged(appWidgetId, R.id.list_view)
                }
            }
        }
        super.onReceive(context, intent)
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        setUp()
        val thisWidget = ComponentName(context, providerClass)
        val allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)

        for (widgetId in allWidgetIds) {
            val options = appWidgetManager.getAppWidgetOptions(widgetId)
            appWidgetManager.partiallyUpdateAppWidget(
                widgetId,
                sizeRemoteViews(context, options, widgetId)
            )
        }

        for (appWidgetId in appWidgetIds) {
            val intent = Intent(context, serviceClass)
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            intent.data = Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME))
            val rv = RemoteViews(context.packageName, R.layout.widget_task_list)
            rv.setRemoteAdapter(R.id.list_view, intent)
            rv.setEmptyView(R.id.list_view, R.id.emptyView)
            rv.setTextViewText(R.id.widget_title, context.getString(titleResId))

            // if the user click on the title: open App
            val openAppIntent = Intent(context.applicationContext, MainActivity::class.java)
            val openApp = PendingIntent.getActivity(context, 0, openAppIntent, withImmutableFlag(PendingIntent.FLAG_UPDATE_CURRENT))
            rv.setOnClickPendingIntent(R.id.widget_title, openApp)

            val taskIntent = Intent(context, providerClass)
            taskIntent.action = DAILY_ACTION
            taskIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            intent.data = Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME))
            val toastPendingIntent = PendingIntent.getBroadcast(
                context, 0, taskIntent,
                withMutableFlag(PendingIntent.FLAG_UPDATE_CURRENT)
            )
            rv.setPendingIntentTemplate(R.id.list_view, toastPendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, rv)

            AppWidgetManager.getInstance(context).notifyAppWidgetViewDataChanged(appWidgetId, R.id.list_view)
        }

        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }

    override fun layoutResourceId(): Int {
        return R.layout.widget_task_list
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
        const val DAILY_ACTION = "com.habitrpg.android.habitica.DAILY_ACTION"
        const val TASK_ID_ITEM = "com.habitrpg.android.habitica.TASK_ID_ITEM"
    }
}
