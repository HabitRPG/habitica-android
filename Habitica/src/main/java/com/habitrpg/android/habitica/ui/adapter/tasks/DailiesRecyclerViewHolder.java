package com.habitrpg.android.habitica.ui.adapter.tasks;

import com.habitrpg.android.habitica.HabiticaBaseApplication;
import com.habitrpg.android.habitica.components.AppComponent;
import com.habitrpg.android.habitica.helpers.TaskFilterHelper;
import com.habitrpg.android.habitica.ui.viewHolders.tasks.DailyViewHolder;

import android.content.Context;
import android.support.annotation.Nullable;
import android.view.ViewGroup;

public class DailiesRecyclerViewHolder extends SortableTasksRecyclerViewAdapter<DailyViewHolder> {

    public int dailyResetOffset;

    public DailiesRecyclerViewHolder(String taskType, TaskFilterHelper taskFilterHelper, int layoutResource,
                                     Context newContext, String userID, int dailyResetOffset,
                                     @Nullable SortTasksCallback sortTasksCallback) {
        super(taskType, taskFilterHelper, layoutResource, newContext, userID, sortTasksCallback);
        this.dailyResetOffset = dailyResetOffset;
    }

    @Override
    public DailyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new DailyViewHolder(getContentView(parent), dailyResetOffset);
    }


    @Override
    protected void injectThis(AppComponent component) {
        HabiticaBaseApplication.getComponent().inject(this);
    }
}
