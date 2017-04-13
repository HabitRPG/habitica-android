package com.habitrpg.android.habitica.widget;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.models.tasks.Task;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;

public class AddTaskWidgetProvider extends BaseWidgetProvider {

    @Override
    public int layoutResourceId() {
        return R.layout.widget_add_task;
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // Get all ids
        ComponentName thisWidget = new ComponentName(context,
                AddTaskWidgetProvider.class);
        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

        if (Build.VERSION.SDK_INT >= 16) {
            for (int widgetId : allWidgetIds) {
                Bundle options = appWidgetManager.getAppWidgetOptions(widgetId);
                appWidgetManager.partiallyUpdateAppWidget(widgetId,
                        sizeRemoteViews(context, options, widgetId));
            }
        }
    }

    @Override
    public RemoteViews configureRemoteViews(RemoteViews remoteViews, int widgetId, int columns, int rows) {

        String selectedTaskType = getSelectedTaskType(widgetId);
        String addText = "";
        int backgroundResource = R.drawable.widget_add_habit_background;
        switch (selectedTaskType) {
            case Task.TYPE_HABIT:
                addText = this.context.getResources().getString(R.string.add_habit);
                backgroundResource = R.drawable.widget_add_habit_background;
                break;
            case Task.TYPE_DAILY:
                addText = this.context.getResources().getString(R.string.add_daily);
                backgroundResource = R.drawable.widget_add_daily_background;
                break;
            case Task.TYPE_TODO:
                addText = this.context.getResources().getString(R.string.add_todo);
                backgroundResource = R.drawable.widget_add_todo_background;
                break;
            case Task.TYPE_REWARD:
                addText = this.context.getResources().getString(R.string.add_reward);
                backgroundResource = R.drawable.widget_add_reward_background;
                break;
        }
        remoteViews.setTextViewText(R.id.add_task_text, addText);
        remoteViews.setInt(R.id.add_task_icon, "setBackgroundResource", backgroundResource);
        return remoteViews;
    }

    private String getSelectedTaskType(int widgetId) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this.context);
        return preferences.getString("add_task_widget_" + widgetId, Task.TYPE_HABIT);
    }
}
