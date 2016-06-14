package com.habitrpg.android.habitica.ui.adapter.tasks;

import com.habitrpg.android.habitica.helpers.TagsHelper;
import com.habitrpg.android.habitica.ui.viewHolders.tasks.TodoViewHolder;

import android.content.Context;
import android.view.ViewGroup;

public class TodosRecyclerViewAdapter extends BaseTasksRecyclerViewAdapter<TodoViewHolder> {

    public TodosRecyclerViewAdapter(String taskType, TagsHelper tagsHelper, int layoutResource, Context newContext, String userID) {
        super(taskType, tagsHelper, layoutResource, newContext, userID);
    }

    @Override
    public TodoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new TodoViewHolder(getContentView(parent));
    }
}
