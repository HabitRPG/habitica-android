package com.habitrpg.android.habitica.ui.adapter.tasks;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.models.tasks.ChecklistItem;
import com.habitrpg.android.habitica.ui.helpers.ItemTouchHelperAdapter;
import com.habitrpg.android.habitica.ui.helpers.ItemTouchHelperViewHolder;

import net.pherth.android.emoji_library.EmojiEditText;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CheckListAdapter extends RecyclerView.Adapter<CheckListAdapter.ItemViewHolder>
        implements ItemTouchHelperAdapter {

    private final List<ChecklistItem> items = new ArrayList<>();

    public CheckListAdapter() {
    }

    public void setItems(List<ChecklistItem> items) {
        this.items.addAll(items);
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.checklist_item, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ItemViewHolder holder, int position) {
        holder.textWatcher.id = null;
        holder.checkListTextView.setText(items.get(position).getText());
        holder.textWatcher.id = items.get(position).getId();
    }

    public void addItem(ChecklistItem item) {
        items.add(item);
        notifyItemInserted(items.size() - 1);
    }

    public List<ChecklistItem> getCheckListItems() {
        return items;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }


    @Override
    public void onItemDismiss(int position) {
        if (position >= 0 && position < items.size()) {
            items.remove(position);
            notifyItemRemoved(position);
        }
    }


    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        ChecklistItem item = items.get(fromPosition);
        items.remove(fromPosition);
        items.add(toPosition, item);
        notifyItemMoved(fromPosition, toPosition);
    }

    class ItemViewHolder extends RecyclerView.ViewHolder implements
            ItemTouchHelperViewHolder, Button.OnClickListener {

        ChecklistTextWatcher textWatcher;
        @BindView(R.id.item_edittext)
        EmojiEditText checkListTextView;
        @BindView(R.id.delete_item_button)
        Button deleteButton;

        ItemViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            deleteButton.setOnClickListener(this);

            textWatcher = new ChecklistTextWatcher();
            checkListTextView.addTextChangedListener(textWatcher);
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

        private class ChecklistTextWatcher implements TextWatcher {

            public String id;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (id == null) {
                    return;
                }
                for (ChecklistItem item : items) {
                    if (id.equals(item.getId())) {
                        item.setText(checkListTextView.getText().toString());
                        break;
                    }
                }
            }
        }
    }
}