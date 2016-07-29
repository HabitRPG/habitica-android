package com.habitrpg.android.habitica.ui.adapter.tasks;

import com.habitrpg.android.habitica.helpers.TagsHelper;
import com.habitrpg.android.habitica.ui.viewHolders.tasks.DailyViewHolder;

import android.content.Context;
import android.view.ViewGroup;

public class DailiesRecyclerViewHolder extends SortableTasksRecyclerViewAdapter<DailyViewHolder> {

    public int dailyResetOffset;

    public DailiesRecyclerViewHolder(String taskType, TagsHelper tagsHelper, int layoutResource,
                                     Context newContext, String userID, int dailyResetOffset,
                                     SortTasksCallback sortTasksCallback) {
        super(taskType, tagsHelper, layoutResource, newContext, userID, sortTasksCallback);
        this.dailyResetOffset = dailyResetOffset;
    }

    @Override
    public DailyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new DailyViewHolder(getContentView(parent), dailyResetOffset);
    }

    @Override
    public void onBindViewHolder(DailyViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
    }
}
