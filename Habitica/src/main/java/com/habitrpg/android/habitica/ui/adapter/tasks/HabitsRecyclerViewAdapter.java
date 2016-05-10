package com.habitrpg.android.habitica.ui.adapter.tasks;

import com.habitrpg.android.habitica.helpers.TagsHelper;
import com.habitrpg.android.habitica.ui.viewHolders.tasks.HabitViewHolder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class HabitsRecyclerViewAdapter extends BaseTasksRecyclerViewAdapter<HabitViewHolder> {
    public HabitsRecyclerViewAdapter(String taskType, TagsHelper tagsHelper, int layoutResource, Context newContext) {
        super(taskType, tagsHelper, layoutResource, newContext);
    }

    @Override
    public HabitViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new HabitViewHolder(getContentView(parent));
    }
}