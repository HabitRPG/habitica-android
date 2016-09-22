package com.habitrpg.android.habitica.widget;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.RemoteViews;

import com.habitrpg.android.habitica.HabiticaApplication;
import com.habitrpg.android.habitica.HostConfig;
import com.habitrpg.android.habitica.R;
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
    private AppWidgetManager appWidgetManager;

    private Map<String, Integer> taskMapping;

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        HabiticaApplication application = (HabiticaApplication) getApplication();
        application.getComponent().inject(this);
        this.appWidgetManager = AppWidgetManager.getInstance(this);

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
        } else {
            remoteViews.setViewVisibility(R.id.btnPlusWrapper, View.VISIBLE);

            remoteViews.setInt(R.id.btnPlus, "setBackgroundColor", resources.getColor(task.getLightTaskColor()));
        }
        if (!task.getDown()) {
            remoteViews.setViewVisibility(R.id.btnMinusWrapper, View.GONE);
        } else {
            remoteViews.setViewVisibility(R.id.btnMinusWrapper, View.VISIBLE);
            remoteViews.setInt(R.id.btnMinus, "setBackgroundColor", resources.getColor(task.getMediumTaskColor()));
        }

        appWidgetManager.partiallyUpdateAppWidget(taskMapping.get(task.getId()), remoteViews);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void makeTaskMapping() {
        ComponentName thisWidget = new ComponentName(this, HabitButtonWidgetProvider.class);
        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
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
}
