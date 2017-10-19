package com.habitrpg.android.habitica.ui.adapter.tasks;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import com.habitrpg.android.habitica.helpers.TaskFilterHelper;
import com.habitrpg.android.habitica.models.tasks.Task;
import com.habitrpg.android.habitica.ui.viewHolders.tasks.BaseTaskViewHolder;

import io.realm.OrderedCollectionChangeSet;
import io.realm.OrderedRealmCollection;
import io.realm.OrderedRealmCollectionChangeListener;
import io.realm.RealmList;
import io.realm.RealmModel;
import io.realm.RealmQuery;
import io.realm.RealmRecyclerViewAdapter;
import io.realm.RealmResults;

public abstract class RealmBaseTasksRecyclerViewAdapter<VH extends BaseTaskViewHolder> extends RecyclerView.Adapter<VH> implements TaskRecyclerViewAdapter {

        private final boolean hasAutoUpdates;
        private final boolean updateOnModification;
        private boolean ignoreUpdates;
        private final OrderedRealmCollectionChangeListener listener;
        @Nullable
        private OrderedRealmCollection<Task> adapterData;

        private OrderedRealmCollectionChangeListener createListener() {
            return new OrderedRealmCollectionChangeListener() {
                @Override
                public void onChange(Object collection, OrderedCollectionChangeSet changeSet) {
                    if (ignoreUpdates) {
                        return;
                    }
                    // null Changes means the async query returns the first time.
                    if (changeSet == null) {
                        notifyDataSetChanged();
                        return;
                    }
                    // For deletions, the adapter has to be notified in reverse order.
                    OrderedCollectionChangeSet.Range[] deletions = changeSet.getDeletionRanges();
                    for (int i = deletions.length - 1; i >= 0; i--) {
                        OrderedCollectionChangeSet.Range range = deletions[i];
                        notifyItemRangeRemoved(range.startIndex, range.length);
                    }

                    OrderedCollectionChangeSet.Range[] insertions = changeSet.getInsertionRanges();
                    for (OrderedCollectionChangeSet.Range range : insertions) {
                        notifyItemRangeInserted(range.startIndex, range.length);
                    }

                    if (!updateOnModification) {
                        return;
                    }

                    OrderedCollectionChangeSet.Range[] modifications = changeSet.getChangeRanges();
                    for (OrderedCollectionChangeSet.Range range : modifications) {
                        notifyItemRangeChanged(range.startIndex, range.length);
                    }
                }
            };
        }

        public RealmBaseTasksRecyclerViewAdapter(@Nullable OrderedRealmCollection<Task> data, boolean autoUpdate, int layoutResource, @Nullable TaskFilterHelper taskFilterHelper) {
            this.unfilteredData = data;
            this.layoutResource = layoutResource;
            this.taskFilterHelper = taskFilterHelper;
            if (data != null && !data.isManaged())
                throw new IllegalStateException("Only use this adapter with managed RealmCollection, " +
                        "for un-managed lists you can just use the BaseRecyclerViewAdapter");
            this.adapterData = data;
            this.hasAutoUpdates = autoUpdate;
            this.listener = hasAutoUpdates ? createListener() : null;
            this.updateOnModification = true;
        }

        @Override
        public void onAttachedToRecyclerView(final RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
            if (hasAutoUpdates && isDataValid()) {
                //noinspection ConstantConditions
                addListener(adapterData);
            }
        }

        @Override
        public void onDetachedFromRecyclerView(final RecyclerView recyclerView) {
            super.onDetachedFromRecyclerView(recyclerView);
            if (hasAutoUpdates && isDataValid()) {
                //noinspection ConstantConditions
                removeListener(adapterData);
            }
        }

        /**
         * Returns the current ID for an item. Note that item IDs are not stable so you cannot rely on the item ID being the
         * same after notifyDataSetChanged() or {@link #updateData(OrderedRealmCollection)} has been called.
         *
         * @param index position of item in the adapter.
         * @return current item ID.
         */
        @Override
        public long getItemId(final int index) {
            return index;
        }

        @Override
        public int getItemCount() {
            //noinspection ConstantConditions
            return isDataValid() ? adapterData.size() : 0;
        }

        /**
         * Returns the item associated with the specified position.
         * Can return {@code null} if provided Realm instance by {@link OrderedRealmCollection} is closed.
         *
         * @param index index of the item.
         * @return the item at the specified position, {@code null} if adapter data is not valid.
         */
        @SuppressWarnings("WeakerAccess")
        @Nullable
        public Task getItem(int index) {
            //noinspection ConstantConditions
            return isDataValid() ? adapterData.get(index) : null;
        }

        /**
         * Returns data associated with this adapter.
         *
         * @return adapter data.
         */
        @Nullable
        public OrderedRealmCollection<Task> getData() {
            return adapterData;
        }

        /**
         * Updates the data associated to the Adapter. Useful when the query has been changed.
         * If the query does not change you might consider using the automaticUpdate feature.
         *
         * @param data the new {@link OrderedRealmCollection} to display.
         */
        @SuppressWarnings("WeakerAccess")
        public void updateData(@Nullable OrderedRealmCollection<Task> data) {
            if (hasAutoUpdates) {
                if (isDataValid()) {
                    //noinspection ConstantConditions
                    removeListener(adapterData);
                }
                if (data != null) {
                    addListener(data);
                }
            }

            this.adapterData = data;
            notifyDataSetChanged();
        }

        private void addListener(@NonNull OrderedRealmCollection<Task> data) {
            if (data instanceof RealmResults) {
                RealmResults<Task> results = (RealmResults<Task>) data;
                //noinspection unchecked
                results.addChangeListener(listener);
            } else if (data instanceof RealmList) {
                RealmList<Task> list = (RealmList<Task>) data;
                //noinspection unchecked
                list.addChangeListener(listener);
            } else {
                throw new IllegalArgumentException("RealmCollection not supported: " + data.getClass());
            }
        }

        private void removeListener(@NonNull OrderedRealmCollection<Task> data) {
            if (data instanceof RealmResults) {
                RealmResults<Task> results = (RealmResults<Task>) data;
                //noinspection unchecked
                results.removeChangeListener(listener);
            } else if (data instanceof RealmList) {
                RealmList<Task> list = (RealmList<Task>) data;
                //noinspection unchecked
                list.removeChangeListener(listener);
            } else {
                throw new IllegalArgumentException("RealmCollection not supported: " + data.getClass());
            }
        }

        private boolean isDataValid() {
            return adapterData != null && adapterData.isValid();
        }


    private final int layoutResource;
    private final TaskFilterHelper taskFilterHelper;
    private final OrderedRealmCollection<Task> unfilteredData;

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

    @Override
    public void filter() {
        if (unfilteredData == null) {
            return;
        }

        if (taskFilterHelper != null) {
            RealmQuery<Task> query = taskFilterHelper.createQuery(unfilteredData);
            updateData(query.findAllSorted("position"));

        }
    }

    @Override
    public boolean getIgnoreUpdates() {
        return ignoreUpdates;
    }

    @Override
    public void setIgnoreUpdates(boolean ignoreUpdates) {
        this.ignoreUpdates = ignoreUpdates;
    }
}
