package com.habitrpg.android.habitica.widget;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.RemoteViews;

import com.habitrpg.android.habitica.R;

public class HabitButtonWidgetProvider extends BaseWidgetProvider {
    public static String TASK_ID = "TASK_ID";

    @Override
    public int layoutResourceId() {
        return R.layout.widget_habit_button;
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // Get all ids
        ComponentName thisWidget = new ComponentName(context,
                HabitButtonWidgetProvider.class);
        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

        if (Build.VERSION.SDK_INT >= 16) {
            for (int widgetId : allWidgetIds) {
                Bundle options = appWidgetManager.getAppWidgetOptions(widgetId);
                appWidgetManager.partiallyUpdateAppWidget(widgetId,
                        sizeRemoteViews(context, options, widgetId));
            }
        }

        // Build the intent to call the service
        Intent intent = new Intent(context.getApplicationContext(),
                HabitButtonWidgetService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds);

        context.startService(intent);
    }

    @Override
    public RemoteViews configureRemoteViews(RemoteViews remoteViews, int widgetId, int columns, int rows) {
        return remoteViews;
    }
}
