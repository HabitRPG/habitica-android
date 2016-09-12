package com.habitrpg.android.habitica.widget;

import com.habitrpg.android.habitica.R;

import android.annotation.TargetApi;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

public class AvatarStatsWidgetProvider extends BaseWidgetProvider {
    private static final String LOG = AvatarStatsWidgetProvider.class.getName();

    @Override
    public int layoutResourceId() {
        return R.layout.widget_avatar_stats;
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // Get all ids
        ComponentName thisWidget = new ComponentName(context,
                AvatarStatsWidgetProvider.class);
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
                AvatarStatsWidgetService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds);

        context.startService(intent);
    }


    @Override
    public RemoteViews configureRemoteViews(RemoteViews remoteViews, int widgetId, int columns, int rows) {
        if (columns > 3) {
            remoteViews.setViewVisibility(R.id.avatar_view, View.VISIBLE);
        } else {
            remoteViews.setViewVisibility(R.id.avatar_view, View.GONE);
        }

        if (rows > 1) {
            remoteViews.setViewVisibility(R.id.mp_wrapper, View.VISIBLE);
            remoteViews.setViewVisibility(R.id.detail_info_view, View.VISIBLE);
        } else {
            remoteViews.setViewVisibility(R.id.mp_wrapper, View.GONE);
            remoteViews.setViewVisibility(R.id.detail_info_view, View.GONE);
        }

        return remoteViews;
    }
}
