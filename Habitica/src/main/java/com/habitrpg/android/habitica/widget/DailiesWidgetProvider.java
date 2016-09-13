package com.habitrpg.android.habitica.widget;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.RemoteViews;

import com.habitrpg.android.habitica.R;

public class DailiesWidgetProvider extends BaseWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // Get all ids
        ComponentName thisWidget = new ComponentName(context,
                DailiesWidgetProvider.class);
        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

        if (Build.VERSION.SDK_INT >= 16) {
            for (int widgetId : allWidgetIds) {
                Bundle options = appWidgetManager.getAppWidgetOptions(widgetId);
                appWidgetManager.partiallyUpdateAppWidget(widgetId,
                        sizeRemoteViews(context, options, widgetId));
            }
        }

        for (int i = 0; i < appWidgetIds.length; ++i) {
            Intent intent = new Intent(context, DailiesWidgetService.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_dailies);
            rv.setRemoteAdapter(appWidgetIds[i], R.id.list_view, intent);
            rv.setEmptyView(R.id.list, R.id.empty_view);
            appWidgetManager.updateAppWidget(appWidgetIds[i], rv);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public int layoutResourceId() {
        return R.layout.widget_dailies;
    }

    @Override
    public RemoteViews configureRemoteViews(RemoteViews remoteViews, int widgetId, int columns, int rows) {
        return remoteViews;
    }
}
