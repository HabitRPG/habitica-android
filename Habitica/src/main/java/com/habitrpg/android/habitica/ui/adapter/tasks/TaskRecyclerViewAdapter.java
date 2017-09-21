package com.habitrpg.android.habitica.ui.adapter.tasks;

import com.habitrpg.android.habitica.models.tasks.Task;

import io.realm.OrderedRealmCollection;

public interface TaskRecyclerViewAdapter {

    void updateData(OrderedRealmCollection<Task> tasks);

    void filter();

    int getItemViewType(int position);

}
