package com.habitrpg.android.habitica.ui.adapter.tasks;

import com.habitrpg.android.habitica.HabiticaBaseApplication;
import com.habitrpg.android.habitica.components.AppComponent;
import com.habitrpg.android.habitica.helpers.TagsHelper;
import com.habitrpg.android.habitica.ui.viewHolders.tasks.HabitViewHolder;

import android.content.Context;
import android.view.ViewGroup;

public class HabitsRecyclerViewAdapter extends SortableTasksRecyclerViewAdapter<HabitViewHolder> {


    public HabitsRecyclerViewAdapter(String taskType, TagsHelper tagsHelper, int layoutResource, Context newContext, String userID, SortTasksCallback sortCallback) {
        super(taskType, tagsHelper, layoutResource, newContext, userID, sortCallback);
    }

    @Override
    public HabitViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new HabitViewHolder(getContentView(parent));
    }

    @Override
    protected void injectThis(AppComponent component) {
        HabiticaBaseApplication.getComponent().inject(this);
    }
}