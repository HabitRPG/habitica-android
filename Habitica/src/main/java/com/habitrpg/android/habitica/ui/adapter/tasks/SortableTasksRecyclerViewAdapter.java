package com.habitrpg.android.habitica.ui.adapter.tasks;

import com.habitrpg.android.habitica.helpers.TaskFilterHelper;
import com.habitrpg.android.habitica.ui.helpers.ItemTouchHelperAdapter;
import com.habitrpg.android.habitica.ui.helpers.ItemTouchHelperDropCallback;
import com.habitrpg.android.habitica.ui.viewHolders.tasks.BaseTaskViewHolder;
import com.habitrpg.android.habitica.models.tasks.Task;

import android.content.Context;
import android.support.annotation.Nullable;

import java.util.Collections;

/**
 * Created by ell on 7/21/16.
 */
public abstract class SortableTasksRecyclerViewAdapter<VH extends BaseTaskViewHolder>
        extends BaseTasksRecyclerViewAdapter<VH> implements ItemTouchHelperAdapter, ItemTouchHelperDropCallback {

    private SortTasksCallback sortCallback;

    public SortableTasksRecyclerViewAdapter(String taskType, TaskFilterHelper taskFilterHelper, int layoutResource,
                                            Context newContext, String userID, @Nullable SortTasksCallback sortCallback) {
        super(taskType, taskFilterHelper, layoutResource, newContext, userID);
        this.sortCallback = sortCallback;
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        if (filteredContent.size() < fromPosition || filteredContent.size() < toPosition) {
            return;
        }
        Collections.swap(filteredContent, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public void onItemDismiss(int position) {
        //NO OP
    }

    @Override
    public void onDrop(int from, int to) {
        if (this.sortCallback != null && from != to) {
            this.sortCallback.onMove(filteredContent.get(to), from, to);
        }
    }

    public interface SortTasksCallback {
        void onMove(Task task, int from, int to);
    }
}
