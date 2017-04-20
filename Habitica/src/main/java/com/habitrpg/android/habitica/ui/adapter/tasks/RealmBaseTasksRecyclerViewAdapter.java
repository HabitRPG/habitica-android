package com.habitrpg.android.habitica.ui.adapter.tasks;

import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.habitrpg.android.habitica.models.tasks.Task;
import com.habitrpg.android.habitica.ui.viewHolders.tasks.BaseTaskViewHolder;

import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;

public abstract class RealmBaseTasksRecyclerViewAdapter<VH extends BaseTaskViewHolder> extends RealmRecyclerViewAdapter<Task, VH> {

    private final int layoutResource;

    public RealmBaseTasksRecyclerViewAdapter(@Nullable OrderedRealmCollection<Task> data, boolean autoUpdate, int layoutResource) {
        super(data, autoUpdate);
        this.layoutResource = layoutResource;
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(VH holder, int position) {
        Task item = getItem(position);
        if (item != null) {
            holder.bindHolder(item, position);
        }
    }

    View getContentView(ViewGroup parent) {
        return getContentView(parent, layoutResource);
    }

    protected View getContentView(ViewGroup parent, int layoutResource) {
        return LayoutInflater.from(parent.getContext()).inflate(layoutResource, parent, false);
    }
}
