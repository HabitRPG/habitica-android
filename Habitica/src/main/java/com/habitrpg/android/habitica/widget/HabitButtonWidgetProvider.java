package com.habitrpg.android.habitica.widget;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.RemoteViews;

import com.habitrpg.android.habitica.HabiticaBaseApplication;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.data.TaskRepository;
import com.habitrpg.android.habitica.helpers.RxErrorHandler;
import com.habitrpg.android.habitica.models.responses.TaskDirection;
import com.habitrpg.android.habitica.modules.AppModule;

import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Named;

public class HabitButtonWidgetProvider extends BaseWidgetProvider {

    public static final String HABIT_ACTION = "com.habitrpg.android.habitica.HABIT_ACTION";
    public static final String TASK_ID = "com.habitrpg.android.habitica.TASK_ID_ITEM";
    public static final String TASK_DIRECTION = "com.habitrpg.android.habitica.TASK_DIRECTION";
    @Inject
    public TaskRepository taskRepository;
    @Inject
    @Named(AppModule.NAMED_USER_ID)
    public String userId;

    private void setUp() {
        if (taskRepository == null) {
            Objects.requireNonNull(HabiticaBaseApplication.Companion.getUserComponent()).inject(this);
        }
    }

    @Override
    public int layoutResourceId() {
        return R.layout.widget_habit_button;
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        setUp();
        ComponentName thisWidget = new ComponentName(context,
                HabitButtonWidgetProvider.class);
        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

        for (int widgetId : allWidgetIds) {
            Bundle options = appWidgetManager.getAppWidgetOptions(widgetId);
            appWidgetManager.partiallyUpdateAppWidget(widgetId,
                    sizeRemoteViews(context, options, widgetId));
        }

        // Build the intent to call the service
        Intent intent = new Intent(context.getApplicationContext(), HabitButtonWidgetService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds);

        try {
            context.startService(intent);
        } catch (IllegalStateException ignore) {
            //TODO: Make this play more nicely with Android 8
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        setUp();
        if (intent.getAction().equals(HABIT_ACTION)) {
            AppWidgetManager mgr = AppWidgetManager.getInstance(context);
            int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
            String taskId = intent.getStringExtra(TASK_ID);
            String direction = intent.getStringExtra(TASK_DIRECTION);

            int[] ids = {appWidgetId};

            if (taskId != null) {
                getUserRepository().getUser(userId).firstElement().flatMap(user -> taskRepository.taskChecked(user, taskId, TaskDirection.UP.getText().equals(direction), false, null))
                        .subscribe(taskDirectionData -> showToastForTaskDirection(context, taskDirectionData, userId), RxErrorHandler.Companion.handleEmptyError(), () -> this.onUpdate(context, mgr, ids));
            }
        }
        super.onReceive(context, intent);
    }

    @Override
    public RemoteViews configureRemoteViews(RemoteViews remoteViews, int widgetId, int columns, int rows) {
        return remoteViews;
    }
}
