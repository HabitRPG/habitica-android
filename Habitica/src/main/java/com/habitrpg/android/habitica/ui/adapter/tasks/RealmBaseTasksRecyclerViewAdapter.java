package com.habitrpg.android.habitica.ui.adapter.tasks;

import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import com.habitrpg.android.habitica.helpers.TaskFilterHelper;
import com.habitrpg.android.habitica.models.tasks.Task;
import com.habitrpg.android.habitica.ui.viewHolders.tasks.BaseTaskViewHolder;

import io.realm.OrderedRealmCollection;
import io.realm.RealmQuery;
import io.realm.RealmRecyclerViewAdapter;

public abstract class RealmBaseTasksRecyclerViewAdapter<VH extends BaseTaskViewHolder> extends RealmRecyclerViewAdapter<Task, VH> {

    private final int layoutResource;
    private final TaskFilterHelper taskFilterHelper;
    private final OrderedRealmCollection<Task> unfilteredData;

    public RealmBaseTasksRecyclerViewAdapter(@Nullable OrderedRealmCollection<Task> data, boolean autoUpdate, int layoutResource, @Nullable TaskFilterHelper taskFilterHelper) {
        super(data, autoUpdate);
        this.unfilteredData = data;
                this.layoutResource = layoutResource;
        this.taskFilterHelper = taskFilterHelper;
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

    public void filter() {
        if (unfilteredData == null) {
            return;
        }

        if (taskFilterHelper != null) {
            RealmQuery<Task> query = taskFilterHelper.createQuery(unfilteredData);
            updateData(query.findAllSorted("position"));

        }
    }
}
