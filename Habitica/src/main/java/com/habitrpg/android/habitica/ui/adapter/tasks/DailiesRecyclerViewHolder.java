package com.habitrpg.android.habitica.ui.adapter.tasks;

import com.habitrpg.android.habitica.helpers.TagsHelper;
import com.habitrpg.android.habitica.ui.viewHolders.tasks.BaseTaskViewHolder;
import com.habitrpg.android.habitica.ui.viewHolders.tasks.DailyViewHolder;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

public class DailiesRecyclerViewHolder extends BaseTasksRecyclerViewAdapter<DailyViewHolder> {

    public int dailyResetOffset;

    public DailiesRecyclerViewHolder(String taskType, TagsHelper tagsHelper, int layoutResource, Context newContext, int dailyResetOffset) {
        super(taskType, tagsHelper, layoutResource, newContext);
    }

    @Override
    public DailyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new DailyViewHolder(getContentView(parent), dailyResetOffset);
    }

    @Override
    public void onBindViewHolder(DailyViewHolder holder, int position) {
        holder.displayChecklist = false;
        super.onBindViewHolder(holder, position);
    }
}
