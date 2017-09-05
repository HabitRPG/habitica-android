package com.habitrpg.android.habitica.ui.adapter.setup;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.habitrpg.android.habitica.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TaskSetupAdapter extends RecyclerView.Adapter<TaskSetupAdapter.TaskViewHolder> {

    public List<Boolean> checkedList;
    private String[][] taskList;

    public void setTaskList(String[][] taskList) {
        this.taskList = taskList;
        this.checkedList = new ArrayList<>();
        for (String[] ignored : this.taskList) {
            this.checkedList.add(false);
        }
    }

    @Override
    public TaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.task_setup_item, parent, false);

        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TaskViewHolder holder, int position) {
        holder.bind(this.taskList[position], this.checkedList.get(position));
    }

    @Override
    public int getItemCount() {
        return this.taskList == null ? 0 : this.taskList.length;
    }

    class TaskViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final Drawable icon;
        @BindView(R.id.textView)
        TextView textView;

        String[] taskGroup;
        Boolean isChecked;

        Context context;

        TaskViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);

            context = itemView.getContext();

            itemView.setOnClickListener(this);

            icon = VectorDrawableCompat.create(context.getResources(), R.drawable.ic_check_white_18dp, null);
            if (icon != null) {
                icon.setColorFilter(ContextCompat.getColor(context, R.color.brand_100), PorterDuff.Mode.MULTIPLY);
            }
        }

        public void bind(String[] taskGroup, Boolean isChecked) {
            this.taskGroup = taskGroup;
            this.isChecked = isChecked;

            this.textView.setText(this.taskGroup[0]);
            if (this.isChecked) {
                this.textView.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
                textView.getBackground().setColorFilter(ContextCompat.getColor(context, R.color.white), PorterDuff.Mode.MULTIPLY);
                textView.setTextColor(ContextCompat.getColor(context, R.color.brand_100));
            } else {
                this.textView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                textView.getBackground().setColorFilter(ContextCompat.getColor(context, R.color.brand_100), PorterDuff.Mode.MULTIPLY);
                textView.setTextColor(ContextCompat.getColor(context, R.color.white));
            }
        }

        @Override
        public void onClick(View v) {
            int position = this.getAdapterPosition();
            checkedList.set(position, !checkedList.get(position));
            notifyItemChanged(position);
        }
    }

}
