package com.habitrpg.android.habitica.widget;

import com.habitrpg.android.habitica.HabiticaApplication;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.ui.helpers.MarkdownParser;
import com.habitrpg.android.habitica.models.HabitRPGUser;
import com.habitrpg.android.habitica.models.tasks.Task;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.OrderBy;
import com.raizlabs.android.dbflow.sql.language.Select;

import net.pherth.android.emoji_library.EmojiHandler;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
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
    @Named("UserID")
    public String userID;
    private Integer customDayStart;
    private int listItemResId;
    private int listItemTextResId;
    private String taskType;
    private List<Task> taskList = new ArrayList<>();
    private Context context = null;
    private boolean reloadData;

    public TaskListFactory(Context context, Intent intent, String taskType, int listItemResId, int listItemTextResId) {
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
            Observable.defer(() -> Observable.just(new Select().from(HabitRPGUser.class).where(Condition.column("id").eq(userID)).querySingle()))
                    .subscribe(habitRPGUser -> {
                        customDayStart = habitRPGUser.getPreferences().getDayStart();
                        this.loadData();
                    }, throwable -> {
                    });
        } else {
            this.loadData();
        }
    }

    private void loadData() {
        Observable.defer(() -> Observable.from(new Select()
                .from(Task.class)
                .where(Condition.column("type").eq(taskType))
                .and(Condition.column("completed").eq(false))
                .orderBy(OrderBy.columns("position", "dateCreated").descending())
                .queryList()))
                .filter(task -> task.type.equals(Task.TYPE_TODO) || task.isDisplayedActive(customDayStart))
                .toList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(tasks -> {
                    taskList = tasks;
                    this.reloadData = false;
                    AppWidgetManager.getInstance(context).notifyAppWidgetViewDataChanged(widgetId, R.id.list_view);
                }, throwable -> {
                    this.reloadData = false;
                });
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
