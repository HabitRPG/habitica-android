package com.habitrpg.android.habitica.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.habitrpg.android.habitica.R;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Task;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class DailiesListProvider implements RemoteViewsService.RemoteViewsFactory {
    private final int widgetId;
    private List<Task> taskList = new ArrayList<>();
    private Context context = null;
    private boolean reloadData;

    public DailiesListProvider(Context context, Intent intent) {
        this.context = context;
        this.widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 0);
        this.reloadData = false;
        this.loadData();
    }

    private void loadData() {
        Observable.defer(() -> Observable.from(new Select()
                .from(Task.class)
                .where(Condition.column("type").eq(Task.TYPE_DAILY))
                .and(Condition.column("completed").eq(false))
                .queryList()))
                .filter(task -> task.isDisplayedActive(0))
                .toList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(tasks -> {
                    taskList = tasks;
                    this.reloadData = false;
                    AppWidgetManager.getInstance(context).notifyAppWidgetViewDataChanged(widgetId, R.id.list_view);
                }, throwable -> {});
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
                context.getPackageName(), R.layout.widget_dailies_list_row);
        if (taskList.size() > position) {
            Task task = taskList.get(position);
            remoteView.setTextViewText(R.id.dailies_text, task.text);
            remoteView.setInt(R.id.checkbox_background, "setBackgroundResource", task.getLightTaskColor());
            Intent fillInIntent = new Intent();
            fillInIntent.putExtra(DailiesWidgetProvider.TASK_ID_ITEM, task.getId());
            remoteView.setOnClickFillInIntent(R.id.dailies_list_row, fillInIntent);
        }
        return remoteView;
    }

    @Override
    public RemoteViews getLoadingView() {
        return new RemoteViews(
                context.getPackageName(), R.layout.widget_dailies_list_row);
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