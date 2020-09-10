package com.habitrpg.android.habitica.ui.helpers;

import androidx.recyclerview.widget.RecyclerView;

/**
 * Interface to notify a {@link RecyclerView.Adapter} of moving and dismissal event from a {@link
 * androidx.recyclerview.widget.ItemTouchHelper.Callback}.
 *
 * @author Paul Burke (ipaulpro)
 */
public interface ItemTouchHelperAdapter {
    void onItemMove(int fromPosition, int toPosition);
    void onItemDismiss(int position);
}
