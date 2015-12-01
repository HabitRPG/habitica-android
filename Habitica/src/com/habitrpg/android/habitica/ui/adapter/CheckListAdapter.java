package com.habitrpg.android.habitica.ui.adapter;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.ui.helpers.ItemTouchHelperAdapter;
import com.habitrpg.android.habitica.ui.helpers.ItemTouchHelperViewHolder;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.ChecklistItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by franzejr on 15/11/15.
 */
public class CheckListAdapter extends RecyclerView.Adapter<CheckListAdapter.ItemViewHolder>
    implements ItemTouchHelperAdapter{

    private final List<ChecklistItem> mItems = new ArrayList<>();

    public CheckListAdapter(List<ChecklistItem> checklistItems) {
        mItems.addAll(checklistItems);
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.checklist_item, parent, false);
        ItemViewHolder itemViewHolder = new ItemViewHolder(view);
        return itemViewHolder;
    }

    @Override
    public void onBindViewHolder(final ItemViewHolder holder, int position) {
        holder.checkListTextView.setText(mItems.get(position).getText());
    }

    public void addItem(ChecklistItem item){
        mItems.add(item);
        notifyItemInserted(mItems.size() - 1);
    }

    public List<ChecklistItem> getCheckListItems(){
        return mItems;
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }


    @Override
    public void onItemDismiss(int position) {
        mItems.remove(position);
        notifyItemRemoved(position);
    }


    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        Collections.swap(mItems, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder implements
            ItemTouchHelperViewHolder, Button.OnClickListener {

        @InjectView(R.id.item_edittext)
        EditText checkListTextView;

        @InjectView(R.id.delete_item_button)
        Button deleteButton;

        public ItemViewHolder(View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);
            deleteButton.setOnClickListener(this);

            checkListTextView.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    mItems.get(getAdapterPosition()).setText(checkListTextView.getText().toString());
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
        }

        @Override
        public void onItemSelected() {
            itemView.setBackgroundColor(Color.LTGRAY);
        }

        @Override
        public void onItemClear() {
            itemView.setBackgroundColor(0);
        }

        @Override
        public void onClick(View v) {
            if (v == deleteButton) {
                 CheckListAdapter.this.onItemDismiss(getAdapterPosition());
            }
        }
    }
}