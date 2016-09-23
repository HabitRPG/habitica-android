package com.habitrpg.android.habitica.widget;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.RemoteViews;

import com.habitrpg.android.habitica.APIHelper;
import com.habitrpg.android.habitica.HabiticaApplication;
import com.habitrpg.android.habitica.R;
import com.magicmicky.habitrpgwrapper.lib.models.TaskDirection;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Task;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;

import javax.inject.Inject;

public class HabitButtonWidgetProvider extends BaseWidgetProvider {

    public static final String HABIT_ACTION = "com.habitrpg.android.habitica.HABIT_ACTION";
    public static final String TASK_ID = "com.habitrpg.android.habitica.TASK_ID_ITEM";
    public static final String TASK_DIRECTION = "com.habitrpg.android.habitica.TASK_DIRECTION";
    @Inject
    public APIHelper apiHelper;

    public APIHelper getApiHelper(Context context) {
        if (apiHelper == null) {
            HabiticaApplication application = HabiticaApplication.getInstance(context);
            application.getComponent().inject(this);
        }
        return apiHelper;
    }

    @Override
    public int layoutResourceId() {
        return R.layout.widget_habit_button;
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
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
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(HABIT_ACTION)) {
            AppWidgetManager mgr = AppWidgetManager.getInstance(context);
            int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
            String taskId = intent.getStringExtra(TASK_ID);
            String direction = intent.getStringExtra(TASK_DIRECTION);

            int[] ids = {appWidgetId};

            if (taskId != null) {
                getApiHelper(context).apiService.postTaskDirection(taskId, direction)
                        .compose(getApiHelper(context).configureApiCallObserver())
                        .subscribe(taskDirectionData -> {
                            Task task = new Select().from(Task.class).where(Condition.column("id").eq(taskId)).querySingle();
                            task.value = task.value + taskDirectionData.getDelta();
                            task.save();
                            this.onUpdate(context, mgr, ids);
                        }, throwable -> {
                            this.onUpdate(context, mgr, ids);
                        });
            }
        }
        super.onReceive(context, intent);
    }

    @Override
    public RemoteViews configureRemoteViews(RemoteViews remoteViews, int widgetId, int columns, int rows) {
        return remoteViews;
    }
}
