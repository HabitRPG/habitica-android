package com.habitrpg.android.habitica.ui.viewHolders.tasks;

import com.habitrpg.android.habitica.R;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Task;

import android.view.View;
import android.widget.TextView;

import butterknife.BindView;

public class TodoViewHolder extends ChecklistedViewHolder {

    @BindView(R.id.duedateTextView)
    TextView duedateTextView;

    public TodoViewHolder(View itemView) {
        super(itemView);
    }

    @Override
    public void bindHolder(Task newTask, int position) {
        super.bindHolder(newTask, position);
        if (this.task.getCompleted()) {
            this.checklistIndicatorWrapper.setBackgroundColor(this.taskGray);
        } else {
            this.checklistIndicatorWrapper.setBackgroundResource(this.task.getLightTaskColor());
        }
        if (task.duedate != null) {
            this.duedateTextView.setText(itemView.getContext().getString(R.string.todo_due, task.duedate));
            this.duedateTextView.setVisibility(View.VISIBLE);
        } else {
            this.duedateTextView.setVisibility(View.GONE);
        }
    }

    @Override
    public Boolean shouldDisplayAsActive() {
        return !this.task.getCompleted();
    }
}
