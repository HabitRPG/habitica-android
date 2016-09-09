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

public class AvatarStatsWidgetProvider extends AppWidgetProvider {
    private static final String LOG = AvatarStatsWidgetProvider.class.getName();

    /**
     * Returns number of cells needed for given size of the widget.<br/>
     * see http://stackoverflow.com/questions/14270138/dynamically-adjusting-widgets-content-and-layout-to-the-size-the-user-defined-t
     *
     * @param size Widget size in dp.
     * @return Size in number of cells.
     */
    private static int getCellsForSize(int size) {
        int n = 2;
        while (70 * n - 30 < size) {
            ++n;
        }
        return n - 1;
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
                        configureRemoteViews(context, options));
            }
        }

        // Build the intent to call the service
        Intent intent = new Intent(context.getApplicationContext(),
                AvatarStatsWidgetService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds);

        context.startService(intent);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);

        appWidgetManager.partiallyUpdateAppWidget(appWidgetId,
                configureRemoteViews(context, options));

        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId,
                newOptions);

    }

    /**
     * Determine appropriate view based on width provided.<br/>
     * see http://stackoverflow.com/questions/14270138/dynamically-adjusting-widgets-content-and-layout-to-the-size-the-user-defined-t
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private RemoteViews configureRemoteViews(Context context, Bundle options) {

        int minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
        int minHeight = options
                .getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT);

        // First find out rows and columns based on width provided.
        int rows = getCellsForSize(minHeight);
        int columns = getCellsForSize(minWidth);
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                R.layout.widget_avatar_stats);

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
