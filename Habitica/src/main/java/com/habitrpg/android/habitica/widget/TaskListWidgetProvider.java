package com.habitrpg.android.habitica.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.RemoteViews;

import com.habitrpg.android.habitica.HabiticaBaseApplication;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.data.ApiClient;
import com.habitrpg.android.habitica.data.TaskRepository;
import com.habitrpg.android.habitica.helpers.RxErrorHandler;
import com.habitrpg.android.habitica.modules.AppModule;
import com.habitrpg.android.habitica.ui.activities.MainActivity;

import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Named;

public abstract class TaskListWidgetProvider extends BaseWidgetProvider {
    public static final String DAILY_ACTION = "com.habitrpg.android.habitica.DAILY_ACTION";
    public static final String TASK_ID_ITEM = "com.habitrpg.android.habitica.TASK_ID_ITEM";

    @Inject
    ApiClient apiClient;
    @Inject
    @Named(AppModule.NAMED_USER_ID)
    String userId;
    @Inject
    TaskRepository taskRepository;

    private void setUp(Context context) {
        if (apiClient == null) {
            Objects.requireNonNull(HabiticaBaseApplication.Companion.getUserComponent()).inject(this);
        }
    }

    protected abstract Class getServiceClass();

    protected abstract Class getProviderClass();

    protected abstract int getTitleResId();


    @Override
    public void onReceive(Context context, Intent intent) {
        setUp(context);
        if (intent.getAction().equals(DAILY_ACTION)) {
            int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
            String taskId = intent.getStringExtra(TASK_ID_ITEM);

            if (taskId != null) {
                getUserRepository().getUser(userId).firstElement().flatMap(user -> taskRepository.taskChecked(user, taskId, true, false, null))
                        .subscribe(taskDirectionData -> {
                            taskRepository.markTaskCompleted(taskId, true);
                            showToastForTaskDirection(context, taskDirectionData, userId);
                            AppWidgetManager.getInstance(context).notifyAppWidgetViewDataChanged(appWidgetId, R.id.list_view);
                        }, RxErrorHandler.Companion.handleEmptyError());
            }
        }
        super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        setUp(context);
        ComponentName thisWidget = new ComponentName(context, getProviderClass());
        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

        if (Build.VERSION.SDK_INT >= 16) {
            for (int widgetId : allWidgetIds) {
                Bundle options = appWidgetManager.getAppWidgetOptions(widgetId);
                appWidgetManager.partiallyUpdateAppWidget(widgetId,
                        sizeRemoteViews(context, options, widgetId));
            }
        }

        for (int appWidgetId : appWidgetIds) {
            Intent intent = new Intent(context, getServiceClass());
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_task_list);
            rv.setRemoteAdapter(appWidgetId, R.id.list_view, intent);
            rv.setEmptyView(R.id.list_view, R.id.emptyView);
            rv.setTextViewText(R.id.widget_title, context.getString(getTitleResId()));

            // if the user click on the title: open App
            Intent openAppIntent = new Intent(context.getApplicationContext(), MainActivity.class);
            PendingIntent openApp = PendingIntent.getActivity(context, 0, openAppIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            rv.setOnClickPendingIntent(R.id.widget_title, openApp);

            Intent taskIntent = new Intent(context, getProviderClass());
            taskIntent.setAction(DAILY_ACTION);
            taskIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
            PendingIntent toastPendingIntent = PendingIntent.getBroadcast(context, 0, taskIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            rv.setPendingIntentTemplate(R.id.list_view, toastPendingIntent);

            appWidgetManager.updateAppWidget(appWidgetId, rv);

            AppWidgetManager.getInstance(context).notifyAppWidgetViewDataChanged(appWidgetId, R.id.list_view);
        }

        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public int layoutResourceId() {
        return R.layout.widget_task_list;
    }

    @Override
    public RemoteViews configureRemoteViews(RemoteViews remoteViews, int widgetId, int columns, int rows) {
        return remoteViews;
    }
}
