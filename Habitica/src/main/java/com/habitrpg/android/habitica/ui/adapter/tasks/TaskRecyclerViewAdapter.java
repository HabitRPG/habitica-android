package com.habitrpg.android.habitica.ui.adapter.tasks;

import com.habitrpg.android.habitica.models.tasks.Task;

import io.realm.OrderedRealmCollection;

public interface TaskRecyclerViewAdapter {

    void updateData(OrderedRealmCollection<Task> tasks);

    void filter();

    void notifyItemMoved(int adapterPosition, int adapterPosition1);
    void notifyDataSetChanged();
    int getItemViewType(int position);

    void setIgnoreUpdates(boolean ignoreUpdates);
    boolean getIgnoreUpdates();

}
