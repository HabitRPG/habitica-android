package com.habitrpg.android.habitica.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.widget.RemoteViews
import androidx.preference.PreferenceManager
import com.habitrpg.android.habitica.R
import com.habitrpg.shared.habitica.models.tasks.TaskType
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddTaskWidgetProvider : BaseWidgetProvider() {

    override fun layoutResourceId(): Int {
        return R.layout.widget_add_task
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        // Get all ids
        val thisWidget = ComponentName(
            context,
            AddTaskWidgetProvider::class.java
        )
        val allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)

        for (widgetId in allWidgetIds) {
            val options = appWidgetManager.getAppWidgetOptions(widgetId)
            appWidgetManager.partiallyUpdateAppWidget(
                widgetId,
                sizeRemoteViews(context, options, widgetId)
            )
        }
    }

    override fun configureRemoteViews(
        remoteViews: RemoteViews,
        widgetId: Int,
        columns: Int,
        rows: Int
    ): RemoteViews {

        val selectedTaskType = getSelectedTaskType(widgetId)
        var addText: String? = ""
        var backgroundResource = R.drawable.widget_add_habit_background
        when (selectedTaskType) {
            TaskType.HABIT -> {
                addText = context?.resources?.getString(R.string.add_habit)
                backgroundResource = R.drawable.widget_add_habit_background
            }
            TaskType.DAILY -> {
                addText = context?.resources?.getString(R.string.add_daily)
                backgroundResource = R.drawable.widget_add_daily_background
            }
            TaskType.TODO -> {
                addText = context?.resources?.getString(R.string.add_todo)
                backgroundResource = R.drawable.widget_add_todo_background
            }
            TaskType.REWARD -> {
                addText = context?.resources?.getString(R.string.add_reward)
                backgroundResource = R.drawable.widget_add_reward_background
            }
        }
        remoteViews.setTextViewText(R.id.add_task_text, addText)
        remoteViews.setInt(R.id.add_task_icon, "setBackgroundResource", backgroundResource)
        return remoteViews
    }

    private fun getSelectedTaskType(widgetId: Int): TaskType {
        val preferences = context?.let { PreferenceManager.getDefaultSharedPreferences(it) }
        return TaskType.from(preferences?.getString("add_task_widget_$widgetId", TaskType.HABIT.value)) ?: TaskType.HABIT
    }
}
