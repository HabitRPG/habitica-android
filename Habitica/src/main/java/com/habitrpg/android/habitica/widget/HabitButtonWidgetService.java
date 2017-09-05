package com.habitrpg.android.habitica.widget;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.text.SpannableStringBuilder;
import android.text.style.DynamicDrawableSpan;
import android.view.View;
import android.widget.RemoteViews;

import com.habitrpg.android.habitica.HabiticaBaseApplication;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.data.TaskRepository;
import com.habitrpg.android.habitica.helpers.RxErrorHandler;
import com.habitrpg.android.habitica.models.responses.TaskDirection;
import com.habitrpg.android.habitica.models.tasks.Task;
import com.habitrpg.android.habitica.modules.AppModule;
import com.habitrpg.android.habitica.ui.helpers.MarkdownParser;

import net.pherth.android.emoji_library.EmojiHandler;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

public class HabitButtonWidgetService extends Service {
    @Inject
    @Named(AppModule.NAMED_USER_ID)
    public String userId;
    @Inject
    public SharedPreferences sharedPreferences;
    @Inject
    public Resources resources;
    @Inject
    public Context context;
    @Inject
    TaskRepository taskRepository;
    private AppWidgetManager appWidgetManager;

    private Map<String, Integer> taskMapping;
    private int[] allWidgetIds;

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        HabiticaBaseApplication.getComponent().inject(this);
        this.appWidgetManager = AppWidgetManager.getInstance(this);
        ComponentName thisWidget = new ComponentName(this, HabitButtonWidgetProvider.class);
        allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
        makeTaskMapping();

        for (String taskid : this.taskMapping.keySet()) {
            taskRepository.getUnmanagedTask(taskid).first().subscribe(this::updateData, RxErrorHandler.handleEmptyError());
        }

        stopSelf();

        return START_STICKY;
    }

    private void updateData(Task task) {
        RemoteViews remoteViews = new RemoteViews(this.getPackageName(), R.layout.widget_habit_button);
        if (task != null && task.isValid()) {
            CharSequence parsedText = MarkdownParser.parseMarkdown(task.text);

            SpannableStringBuilder builder = new SpannableStringBuilder(parsedText);
            EmojiHandler.addEmojis(this.context, builder, 16, DynamicDrawableSpan.ALIGN_BASELINE, 16, 0, -1, false);

            remoteViews.setTextViewText(R.id.habit_title, builder);

            if (!task.getUp()) {
                remoteViews.setViewVisibility(R.id.btnPlusWrapper, View.GONE);
                remoteViews.setOnClickPendingIntent(R.id.btnPlusWrapper, null);
            } else {
                remoteViews.setViewVisibility(R.id.btnPlusWrapper, View.VISIBLE);
                remoteViews.setInt(R.id.btnPlus, "setBackgroundColor", ContextCompat.getColor(context, task.getLightTaskColor()));
                remoteViews.setOnClickPendingIntent(R.id.btnPlusWrapper, getPendingIntent(task.getId(), TaskDirection.up.toString(), taskMapping.get(task.getId())));
            }
            if (!task.getDown()) {
                remoteViews.setViewVisibility(R.id.btnMinusWrapper, View.GONE);
                remoteViews.setOnClickPendingIntent(R.id.btnMinusWrapper, null);
            } else {
                remoteViews.setViewVisibility(R.id.btnMinusWrapper, View.VISIBLE);
                remoteViews.setInt(R.id.btnMinus, "setBackgroundColor", ContextCompat.getColor(context, task.getMediumTaskColor()));
                remoteViews.setOnClickPendingIntent(R.id.btnMinusWrapper, getPendingIntent(task.getId(), TaskDirection.down.toString(), taskMapping.get(task.getId())));
            }
            appWidgetManager.updateAppWidget(taskMapping.get(task.getId()), remoteViews);
        }
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
        return sharedPreferences.getString("habit_button_widget_" + widgetId, "");
    }

    private PendingIntent getPendingIntent(String taskId, String direction, int widgetId) {
        Intent taskIntent = new Intent(context, HabitButtonWidgetProvider.class);
        taskIntent.setAction(HabitButtonWidgetProvider.HABIT_ACTION);
        taskIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        taskIntent.putExtra(HabitButtonWidgetProvider.TASK_ID, taskId);
        taskIntent.putExtra(HabitButtonWidgetProvider.TASK_DIRECTION, direction);
        return PendingIntent.getBroadcast(context, widgetId + direction.hashCode(), taskIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
