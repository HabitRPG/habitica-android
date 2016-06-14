package com.habitrpg.android.habitica.ui.adapter.tasks;

import android.app.TimePickerDialog;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;

import com.github.data5tream.emojilib.EmojiEditText;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.ui.helpers.ItemTouchHelperAdapter;
import com.habitrpg.android.habitica.ui.helpers.ItemTouchHelperViewHolder;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.RemindersItem;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.RemindersItem;

import java.util.Calendar;
import java.util.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by keithholliday on 5/31/16.
 */
public class RemindersAdapter extends RecyclerView.Adapter<RemindersAdapter.ItemViewHolder>
        implements ItemTouchHelperAdapter {
    private final List<RemindersItem> reminders = new ArrayList<>();

    public RemindersAdapter(List<RemindersItem> remindersInc) {
        reminders.addAll(remindersInc);
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.reminder_item, parent, false);
        ItemViewHolder itemViewHolder = new ItemViewHolder(view);
        return itemViewHolder;
    }

    @Override
    public void onBindViewHolder(final ItemViewHolder holder, int position) {
        Date time = reminders.get(position).getTime();
        holder.reminderItemTextView.setText(time.getHours() + ":" + time.getMinutes());
        holder.hour = time.getHours();
        holder.minute = time.getMinutes();
    }

    public void addItem(RemindersItem item){
        reminders.add(item);
        notifyItemInserted(reminders.size() - 1);
    }

    public List<RemindersItem> getRemindersItems(){
        return reminders;
    }

    @Override
    public int getItemCount() {
        return reminders.size();
    }


    @Override
    public void onItemDismiss(int position) {
        if(position >= 0 && position < reminders.size()){
            reminders.get(position).async().delete();
            reminders.remove(position);
            notifyItemRemoved(position);
        }
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        Collections.swap(reminders, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder implements
            ItemTouchHelperViewHolder, Button.OnClickListener {

        @BindView(R.id.item_edittext)
        EditText reminderItemTextView;

        @BindView(R.id.delete_item_button)
        Button deleteButton;

        int hour, minute;

        public ItemViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            deleteButton.setOnClickListener(this);

            reminderItemTextView.setOnClickListener(v -> {
                TimePickerDialog timePicker;
                timePicker = new TimePickerDialog(v.getContext(), (timePicker1, selectedHour, selectedMinute) -> {
                    reminderItemTextView.setText( selectedHour + ":" + selectedMinute);

                    RemindersItem reminder = reminders.get(getAdapterPosition());
                    Date time = reminder.getTime();
                    time.setHours(selectedHour);
                    time.setMinutes(selectedMinute);
                    time.setSeconds(0);
                    reminder.setTime(time);
                }, hour, minute, true);
                timePicker.setTitle("Select Time");
                timePicker.show();
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
                RemindersAdapter.this.onItemDismiss(getAdapterPosition());
            }
        }
    }
}
