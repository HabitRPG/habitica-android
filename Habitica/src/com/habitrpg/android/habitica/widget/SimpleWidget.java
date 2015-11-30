package com.habitrpg.android.habitica.widget;

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

import com.habitrpg.android.habitica.R;

/**
 * Define a simple custom widget for the habitrpg client
 * Created by Mickael on 31/10/13.
 */
public class SimpleWidget extends AppWidgetProvider {
    private static final String LOG = "simplewidgetprovider";

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
        Log.w(LOG, "onUpdate method called");
        // Get all ids
        ComponentName thisWidget = new ComponentName(context,
                SimpleWidget.class);
        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
        // Build the intent to call the service
        Intent intent = new Intent(context.getApplicationContext(),
                UpdateWidgetService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds);

        // Update the widgets via the service
        context.startService(intent);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        Log.v(LOG, "onAppWidgetOptionChanged call");
        Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);

        int minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
        int minHeight = options
                .getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT);

        appWidgetManager.updateAppWidget(appWidgetId,
                getRemoteViews(context, minWidth, minHeight));

        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId,
                newOptions);

    }

    /**
     * Determine appropriate view based on width provided.<br/>
     * see http://stackoverflow.com/questions/14270138/dynamically-adjusting-widgets-content-and-layout-to-the-size-the-user-defined-t
     *
     * @param minWidth
     * @param minHeight
     * @return
     */
    private RemoteViews getRemoteViews(Context context, int minWidth,
                                       int minHeight) {
        // First find out rows and columns based on width provided.
        int rows = getCellsForSize(minHeight);
        int columns = getCellsForSize(minWidth);
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                R.layout.simple_widget);

        if (columns > 2) {
            remoteViews.setViewVisibility(R.id.LL_header, View.VISIBLE);
            // Get 4 column widget remote view and return
        } else {
            remoteViews.setViewVisibility(R.id.LL_header, View.GONE);
        }
        return remoteViews;

    }


}
