package com.habitrpg.android.habitica.widget;

import com.habitrpg.android.habitica.HabiticaApplication;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.data.TaskRepository;
import com.habitrpg.android.habitica.data.UserRepository;
import com.habitrpg.android.habitica.helpers.RxErrorHandler;
import com.habitrpg.android.habitica.models.tasks.Task;
import com.habitrpg.android.habitica.modules.AppModule;
import com.habitrpg.android.habitica.ui.helpers.MarkdownParser;

import net.pherth.android.emoji_library.EmojiHandler;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.text.SpannableStringBuilder;
import android.text.style.DynamicDrawableSpan;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public abstract class TaskListFactory implements RemoteViewsService.RemoteViewsFactory {
    private final int widgetId;
    @Inject
    @Named(AppModule.NAMED_USER_ID)
    public String userID;
    @Inject
    TaskRepository taskRepository;
    @Inject
    UserRepository userRepository;
    private Integer customDayStart;
    private int listItemResId;
    private int listItemTextResId;
    private String taskType;
    private List<Task> taskList = new ArrayList<>();
    private Context context = null;
    private boolean reloadData;

    TaskListFactory(Context context, Intent intent, String taskType, int listItemResId, int listItemTextResId) {
        this.context = context;
        this.widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 0);
        this.listItemResId = listItemResId;
        this.listItemTextResId = listItemTextResId;
        this.reloadData = false;
        this.taskType = taskType;

        if (userID == null) {
            HabiticaApplication.getComponent().inject(this);
        }

        if (customDayStart == null) {
            userRepository.getUser(userID)
                    .subscribe(habitRPGUser -> {
                        customDayStart = habitRPGUser.getPreferences().getDayStart();
                        this.loadData();
                    }, RxErrorHandler.handleEmptyError());
        } else {
            this.loadData();
        }
    }

    private void loadData() {
        Handler mainHandler = new Handler(context.getMainLooper());
        mainHandler.post(() -> taskRepository.getTasks(taskType, userID)
                .first()
                .flatMap(Observable::from)
                .filter(task -> (task.type.equals(Task.TYPE_TODO) && !task.completed) || task.isDisplayedActive(customDayStart))
                .toList()
                .flatMap(tasks -> taskRepository.getTaskCopies(tasks))
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .first()
                .subscribe(tasks -> {
                    taskList = tasks;
                    reloadData = false;
                    AppWidgetManager.getInstance(context).notifyAppWidgetViewDataChanged(widgetId, R.id.list_view);
                }, throwable -> {
                    RxErrorHandler.reportError(throwable);
                    reloadData = false;
                }));

    }


    @Override
    public void onCreate() {
    }

    @Override
    public void onDataSetChanged() {
        if (this.reloadData) {
            this.loadData();
        }
        this.reloadData = true;
    }

    @Override
    public void onDestroy() {
    }

    @Override
    public int getCount() {
        return taskList.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        final RemoteViews remoteView = new RemoteViews(
                context.getPackageName(), listItemResId);
        if (taskList.size() > position) {
            Task task = taskList.get(position);

            CharSequence parsedText = MarkdownParser.parseMarkdown(task.text);

            SpannableStringBuilder builder = new SpannableStringBuilder(parsedText);
            EmojiHandler.addEmojis(this.context, builder, 16, DynamicDrawableSpan.ALIGN_BASELINE, 16, 0, -1, false);

            remoteView.setTextViewText(listItemTextResId, builder);
            remoteView.setInt(R.id.checkbox_background, "setBackgroundResource", task.getLightTaskColor());
            Intent fillInIntent = new Intent();
            fillInIntent.putExtra(TaskListWidgetProvider.TASK_ID_ITEM, task.getId());
            remoteView.setOnClickFillInIntent(R.id.widget_list_row, fillInIntent);
        }
        return remoteView;
    }

    @Override
    public RemoteViews getLoadingView() {
        return new RemoteViews(context.getPackageName(), listItemResId);
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        if (taskList.size() > position) {
            Task task = taskList.get(position);
            return task.getId().hashCode();
        }
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
