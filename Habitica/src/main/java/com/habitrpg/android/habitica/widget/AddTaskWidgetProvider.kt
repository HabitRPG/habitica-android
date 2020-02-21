package com.habitrpg.android.habitica.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.widget.RemoteViews
import androidx.preference.PreferenceManager
import com.habitrpg.android.habitica.R
import com.habitrpg.shared.habitica.models.tasks.Task

class AddTaskWidgetProvider : BaseWidgetProvider() {

    override fun layoutResourceId(): Int {
        return R.layout.widget_add_task
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        // Get all ids
        val thisWidget = ComponentName(context,
                AddTaskWidgetProvider::class.java)
        val allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)

        for (widgetId in allWidgetIds) {
            val options = appWidgetManager.getAppWidgetOptions(widgetId)
            appWidgetManager.partiallyUpdateAppWidget(widgetId,
                    sizeRemoteViews(context, options, widgetId))
        }
    }

    override fun configureRemoteViews(remoteViews: RemoteViews, widgetId: Int, columns: Int, rows: Int): RemoteViews {

        val selectedTaskType = getSelectedTaskType(widgetId)
        var addText: String? = ""
        var backgroundResource = R.drawable.widget_add_habit_background
        when (selectedTaskType) {
            Task.TYPE_HABIT -> {
                addText = context?.resources?.getString(R.string.add_habit)
                backgroundResource = R.drawable.widget_add_habit_background
            }
            Task.TYPE_DAILY -> {
                addText = context?.resources?.getString(R.string.add_daily)
                backgroundResource = R.drawable.widget_add_daily_background
            }
            Task.TYPE_TODO -> {
                addText = context?.resources?.getString(R.string.add_todo)
                backgroundResource = R.drawable.widget_add_todo_background
            }
            Task.TYPE_REWARD -> {
                addText = context?.resources?.getString(R.string.add_reward)
                backgroundResource = R.drawable.widget_add_reward_background
            }
        }
        remoteViews.setTextViewText(R.id.add_task_text, addText)
        remoteViews.setInt(R.id.add_task_icon, "setBackgroundResource", backgroundResource)
        return remoteViews
    }

    private fun getSelectedTaskType(widgetId: Int): String {
        val preferences = PreferenceManager.getDefaultSharedPreferences(this.context)
        return preferences.getString("add_task_widget_$widgetId", Task.TYPE_HABIT) ?: ""
    }
}
