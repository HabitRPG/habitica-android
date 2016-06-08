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
    private final List<RemindersItem> mItems = new ArrayList<>();

    public RemindersAdapter(List<RemindersItem> reminders) {
        mItems.addAll(reminders);
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.reminder_item, parent, false);
        ItemViewHolder itemViewHolder = new ItemViewHolder(view);
        return itemViewHolder;
    }

    @Override
    public void onBindViewHolder(final ItemViewHolder holder, int position) {
        Date time = mItems.get(position).getTime();
        holder.reminderItemTextView.setText(time.getHours() + ":" + time.getMinutes());
    }

    public void addItem(RemindersItem item){
        mItems.add(item);
        notifyItemInserted(mItems.size() - 1);
    }

    public List<RemindersItem> getRemindersItems(){
        return mItems;
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }


    @Override
    public void onItemDismiss(int position) {
        if(position >= 0 && position < mItems.size()){
            mItems.get(position).async().delete();
            mItems.remove(position);
            notifyItemRemoved(position);
        }
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        Collections.swap(mItems, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder implements
            ItemTouchHelperViewHolder, Button.OnClickListener {

        @BindView(R.id.item_edittext)
        EditText reminderItemTextView;

        @BindView(R.id.delete_item_button)
        Button deleteButton;

        public ItemViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            deleteButton.setOnClickListener(this);

            reminderItemTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Calendar mcurrentTime = Calendar.getInstance();
                    int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                    int minute = mcurrentTime.get(Calendar.MINUTE);
                    TimePickerDialog mTimePicker;
                    mTimePicker = new TimePickerDialog(v.getContext(), new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                            reminderItemTextView.setText( selectedHour + ":" + selectedMinute);

                            RemindersItem reminder = mItems.get(getAdapterPosition());
                            Date time = reminder.getTime();
                            time.setHours(selectedHour);
                            time.setMinutes(selectedMinute);
                            time.setSeconds(0);
                            reminder.setTime(time);
                            reminder.async().save();
                        }
                    }, hour, minute, true);
                    mTimePicker.setTitle("Select Time");
                    mTimePicker.show();
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
                RemindersAdapter.this.onItemDismiss(getAdapterPosition());
            }
        }
    }
}
