package com.habitrpg.android.habitica.ui.adapter.tasks;

import android.support.annotation.Nullable;
import android.view.ViewGroup;

import com.habitrpg.android.habitica.helpers.TaskFilterHelper;
import com.habitrpg.android.habitica.models.tasks.Task;
import com.habitrpg.android.habitica.ui.viewHolders.tasks.TodoViewHolder;

import io.realm.OrderedRealmCollection;

public class TodosRecyclerViewAdapter extends RealmBaseTasksRecyclerViewAdapter<TodoViewHolder> {


    public TodosRecyclerViewAdapter(@Nullable OrderedRealmCollection<Task> data, boolean autoUpdate, int layoutResource, TaskFilterHelper taskFilterHelper) {
        super(data, autoUpdate, layoutResource, taskFilterHelper);
    }

    @Override
    public TodoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new TodoViewHolder(getContentView(parent));
    }

}
