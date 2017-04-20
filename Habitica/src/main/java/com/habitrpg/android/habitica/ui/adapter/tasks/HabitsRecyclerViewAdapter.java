package com.habitrpg.android.habitica.ui.adapter.tasks;

import android.content.Context;
import android.support.annotation.Nullable;
import android.view.ViewGroup;

import com.habitrpg.android.habitica.HabiticaBaseApplication;
import com.habitrpg.android.habitica.components.AppComponent;
import com.habitrpg.android.habitica.helpers.TaskFilterHelper;
import com.habitrpg.android.habitica.ui.viewHolders.tasks.HabitViewHolder;

public class HabitsRecyclerViewAdapter extends SortableTasksRecyclerViewAdapter<HabitViewHolder> {


    public HabitsRecyclerViewAdapter(String taskType, TaskFilterHelper taskFilterHelper, int layoutResource, Context newContext, String userID, @Nullable SortTasksCallback sortCallback) {
        super(taskType, taskFilterHelper, layoutResource, newContext, userID, sortCallback);
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