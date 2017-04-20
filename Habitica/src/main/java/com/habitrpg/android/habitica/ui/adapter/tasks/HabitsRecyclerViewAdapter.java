package com.habitrpg.android.habitica.ui.adapter.tasks;

import android.content.Context;
import android.support.annotation.Nullable;
import android.view.ViewGroup;

import com.habitrpg.android.habitica.HabiticaBaseApplication;
import com.habitrpg.android.habitica.components.AppComponent;
import com.habitrpg.android.habitica.helpers.TaskFilterHelper;
import com.habitrpg.android.habitica.models.tasks.Task;
import com.habitrpg.android.habitica.ui.viewHolders.tasks.HabitViewHolder;

import io.realm.OrderedRealmCollection;

public class HabitsRecyclerViewAdapter extends RealmBaseTasksRecyclerViewAdapter<HabitViewHolder> {


    public HabitsRecyclerViewAdapter(@Nullable OrderedRealmCollection<Task> data, boolean autoUpdate, int layoutResource) {
        super(data, autoUpdate, layoutResource);
    }

    @Override
    public HabitViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new HabitViewHolder(getContentView(parent));
    }
}