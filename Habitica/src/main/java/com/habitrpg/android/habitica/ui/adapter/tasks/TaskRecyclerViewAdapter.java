package com.habitrpg.android.habitica.ui.adapter.tasks;

import com.habitrpg.android.habitica.models.tasks.Task;

import org.jetbrains.annotations.Nullable;

import io.realm.OrderedRealmCollection;
import io.realm.RealmResults;

public interface TaskRecyclerViewAdapter {

    void updateData(OrderedRealmCollection<Task> tasks);

    void filter();

    void notifyItemMoved(int adapterPosition, int adapterPosition1);
    void notifyDataSetChanged();
    int getItemViewType(int position);

    void setIgnoreUpdates(boolean ignoreUpdates);
    boolean getIgnoreUpdates();

    void updateUnfilteredData(@Nullable OrderedRealmCollection<Task> data);
}
