package com.habitrpg.android.habitica.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.text.SpannableStringBuilder;
import android.text.style.DynamicDrawableSpan;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.habitrpg.android.habitica.HabiticaApplication;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.data.TaskRepository;
import com.habitrpg.android.habitica.data.UserRepository;
import com.habitrpg.android.habitica.helpers.RxErrorHandler;
import com.habitrpg.android.habitica.models.tasks.Task;
import com.habitrpg.android.habitica.modules.AppModule;
import com.habitrpg.android.habitica.ui.helpers.MarkdownParser;

import net.pherth.android.emoji_library.EmojiHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Named;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;

public abstract class TaskListFactory implements RemoteViewsService.RemoteViewsFactory {
    private final int widgetId;
    @Inject
    @Named(AppModule.NAMED_USER_ID)
    public String userID;
    @Inject
    TaskRepository taskRepository;
    @Inject
    UserRepository userRepository;
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
            Objects.requireNonNull(HabiticaApplication.Companion.getUserComponent()).inject(this);
        }
        this.loadData();
    }

    private void loadData() {
        Handler mainHandler = new Handler(context.getMainLooper());
        mainHandler.post(() -> taskRepository.getTasks(taskType, userID)
                .firstElement()
                .toObservable()
                .flatMap(Observable::fromIterable)
                .filter(task -> (task.getType().equals(Task.TYPE_TODO) && !task.getCompleted()) || task.isDisplayedActive())
                .toList()
                .flatMapMaybe(tasks -> taskRepository.getTaskCopies(tasks).firstElement())
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(tasks -> {
                    reloadData = false;
                    taskList = tasks;
                    AppWidgetManager.getInstance(context).notifyAppWidgetViewDataChanged(widgetId, R.id.list_view);
                }, RxErrorHandler.Companion.handleEmptyError()));

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

            CharSequence parsedText = MarkdownParser.INSTANCE.parseMarkdown(task.getText());

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
