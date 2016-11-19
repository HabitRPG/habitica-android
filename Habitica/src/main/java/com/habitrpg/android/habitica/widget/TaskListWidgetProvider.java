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

import com.habitrpg.android.habitica.APIHelper;
import com.habitrpg.android.habitica.HabiticaApplication;
import com.habitrpg.android.habitica.HostConfig;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.ui.activities.MainActivity;
import com.magicmicky.habitrpgwrapper.lib.models.TaskDirection;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Task;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;

import javax.inject.Inject;

public abstract class TaskListWidgetProvider extends BaseWidgetProvider {
    public static final String DAILY_ACTION = "com.habitrpg.android.habitica.DAILY_ACTION";
    public static final String TASK_ID_ITEM = "com.habitrpg.android.habitica.TASK_ID_ITEM";

    @Inject
    APIHelper apiHelper;
    @Inject
    HostConfig hostConfig;

    private void setUp(Context context) {
        if (apiHelper == null) {
            HabiticaApplication application = HabiticaApplication.getInstance(context);
            application.getComponent().inject(this);
        }
    }

    protected abstract Class getServiceClass();
    protected abstract Class getProviderClass();
    protected abstract int getTitleResId();


    @Override
    public void onReceive(Context context, Intent intent) {
        setUp(context);
        if (intent.getAction().equals(DAILY_ACTION)) {
            AppWidgetManager mgr = AppWidgetManager.getInstance(context);
            int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
            String taskId = intent.getStringExtra(TASK_ID_ITEM);

            if (taskId != null) {
                apiHelper.apiService.postTaskDirection(taskId, TaskDirection.up.toString())
                        .compose(apiHelper.configureApiCallObserver())
                        .subscribe(taskDirectionData -> {
                            Task task = new Select().from(Task.class).where(Condition.column("id").eq(taskId)).querySingle();
                            task.completed = true;
                            task.save();
                            showToastForTaskDirection(context, taskDirectionData, hostConfig.getUser());
                            AppWidgetManager.getInstance(context).notifyAppWidgetViewDataChanged(appWidgetId, R.id.list_view);
                        }, throwable -> {
                            AppWidgetManager.getInstance(context).notifyAppWidgetViewDataChanged(appWidgetId, R.id.list_view);
                        });
            }
        }
        super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
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

        for (int i = 0; i < appWidgetIds.length; ++i) {
            Intent intent = new Intent(context, getServiceClass());
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_task_list);
            rv.setRemoteAdapter(appWidgetIds[i], R.id.list_view, intent);
            rv.setEmptyView(R.id.list, R.id.empty_view);
            rv.setTextViewText(R.id.widget_title, context.getString(getTitleResId()));

            // if the user click on the title: open App
            Intent openAppIntent = new Intent(context.getApplicationContext(), MainActivity.class);
            PendingIntent openApp = PendingIntent.getActivity(context, 0, openAppIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            rv.setOnClickPendingIntent(R.id.widget_title, openApp);

            Intent taskIntent = new Intent(context, getProviderClass());
            taskIntent.setAction(DAILY_ACTION);
            taskIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
            PendingIntent toastPendingIntent = PendingIntent.getBroadcast(context, 0, taskIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            rv.setPendingIntentTemplate(R.id.list_view, toastPendingIntent);

            appWidgetManager.updateAppWidget(appWidgetIds[i], rv);

            AppWidgetManager.getInstance(context).notifyAppWidgetViewDataChanged(appWidgetIds[i], R.id.list_view);
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
