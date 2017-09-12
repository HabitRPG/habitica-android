package com.habitrpg.android.habitica.ui.adapter.tasks;

import android.support.annotation.Nullable;
import android.view.ViewGroup;

import com.habitrpg.android.habitica.helpers.TaskFilterHelper;
import com.habitrpg.android.habitica.models.tasks.Task;
import com.habitrpg.android.habitica.ui.viewHolders.tasks.DailyViewHolder;

import io.realm.OrderedRealmCollection;

public class DailiesRecyclerViewHolder extends RealmBaseTasksRecyclerViewAdapter<DailyViewHolder> {


    public DailiesRecyclerViewHolder(@Nullable OrderedRealmCollection<Task> data, boolean autoUpdate, int layoutResource, int dailyResetOffset, TaskFilterHelper taskFilterHelper) {
        super(data, autoUpdate, layoutResource, taskFilterHelper);
    }

    @Override
    public DailyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new DailyViewHolder(getContentView(parent));
    }
}
