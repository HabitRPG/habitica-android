package com.habitrpg.android.habitica.widget;

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
    private List<Task> taskList = new ArrayList<>();
    private Context context = null;

    public DailiesListProvider(Context context, Intent intent) {
        this.context = context;

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
                    DailiesListProvider.this.notify();
                }, throwable -> {});
    }

    @Override
    public void onCreate() {
    }

    @Override
    public void onDataSetChanged() {
    }

    @Override
    public void onDestroy() {
    }

    @Override
    public int getCount() {
        return taskList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public RemoteViews getViewAt(int position) {
        final RemoteViews remoteView = new RemoteViews(
                context.getPackageName(), R.layout.widget_dailies_list_row);
        Task task = taskList.get(position);
        remoteView.setTextViewText(R.id.dailies_text, task.text);
        return remoteView;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 0;
    }
}