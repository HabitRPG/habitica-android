package com.habitrpg.android.habitica.widget;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.IBinder;
import android.view.View;
import android.widget.RemoteViews;

import com.habitrpg.android.habitica.HabiticaApplication;
import com.habitrpg.android.habitica.HostConfig;
import com.habitrpg.android.habitica.R;
import com.magicmicky.habitrpgwrapper.lib.models.TaskDirection;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Task;
import com.raizlabs.android.dbflow.runtime.transaction.BaseTransaction;
import com.raizlabs.android.dbflow.runtime.transaction.TransactionListener;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

public class HabitButtonWidgetService extends Service {
    @Inject
    public HostConfig hostConfig;
    @Inject
    public SharedPreferences sharedPreferences;
    @Inject
    public Resources resources;
    @Inject
    public Context context;
    private AppWidgetManager appWidgetManager;

    private Map<String, Integer> taskMapping;
    private int[] allWidgetIds;

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        HabiticaApplication application = (HabiticaApplication) getApplication();
        application.getComponent().inject(this);
        this.appWidgetManager = AppWidgetManager.getInstance(this);
        ComponentName thisWidget = new ComponentName(this, HabitButtonWidgetProvider.class);
        allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
        makeTaskMapping();

        for (String taskid : this.taskMapping.keySet()) {
            new Select().from(Task.class).where(Condition.column("id").eq(taskid)).async().querySingle(userTransactionListener);
        }

        stopSelf();

        return START_STICKY;
    }

    private TransactionListener<Task> userTransactionListener = new TransactionListener<Task>() {
        @Override
        public void onResultReceived(Task task) {
            updateData(task);
        }

        @Override
        public boolean onReady(BaseTransaction<Task> task) {
            return true;
        }

        @Override
        public boolean hasResult(BaseTransaction<Task> baseTransaction, Task task) {
            return true;
        }
    };

    private void updateData(Task task) {
        RemoteViews remoteViews = new RemoteViews(this.getPackageName(), R.layout.widget_habit_button);
        remoteViews.setTextViewText(R.id.habit_title, task.text);

        if (!task.getUp()) {
            remoteViews.setViewVisibility(R.id.btnPlusWrapper, View.GONE);
            remoteViews.setOnClickPendingIntent(R.id.btnPlusWrapper, null);
        } else {
            remoteViews.setViewVisibility(R.id.btnPlusWrapper, View.VISIBLE);
            remoteViews.setInt(R.id.btnPlus, "setBackgroundColor", resources.getColor(task.getLightTaskColor()));
            remoteViews.setOnClickPendingIntent(R.id.btnPlusWrapper, getPendingIntent(task.getId(), TaskDirection.up.toString(), taskMapping.get(task.getId())));
        }
        if (!task.getDown()) {
            remoteViews.setViewVisibility(R.id.btnMinusWrapper, View.GONE);
            remoteViews.setOnClickPendingIntent(R.id.btnMinusWrapper, null);
        } else {
            remoteViews.setViewVisibility(R.id.btnMinusWrapper, View.VISIBLE);
            remoteViews.setInt(R.id.btnMinus, "setBackgroundColor", resources.getColor(task.getMediumTaskColor()));
            remoteViews.setOnClickPendingIntent(R.id.btnMinusWrapper, getPendingIntent(task.getId(), TaskDirection.down.toString() , taskMapping.get(task.getId())));
        }

        appWidgetManager.updateAppWidget(taskMapping.get(task.getId()), remoteViews);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void makeTaskMapping() {
        this.taskMapping = new HashMap<>();
        for (int widgetId : allWidgetIds) {
            String taskId = getTaskId(widgetId);
            if (!taskId.equals("")) {
                this.taskMapping.put(taskId, widgetId);
            }
        }
    }

    private String getTaskId(int widgetId) {
        return sharedPreferences.getString("habit_button_widget_"+widgetId, "");
    }

    private PendingIntent getPendingIntent(String taskId, String direction, int widgetId) {
        Intent taskIntent = new Intent(context, HabitButtonWidgetProvider.class);
        taskIntent.setAction(HabitButtonWidgetProvider.HABIT_ACTION);
        taskIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        taskIntent.putExtra(HabitButtonWidgetProvider.TASK_ID, taskId);
        taskIntent.putExtra(HabitButtonWidgetProvider.TASK_DIRECTION, direction);
        return PendingIntent.getBroadcast(context, widgetId+direction.hashCode(), taskIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
