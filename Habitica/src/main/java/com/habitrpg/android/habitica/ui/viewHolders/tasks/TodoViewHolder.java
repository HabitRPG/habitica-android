package com.habitrpg.android.habitica.ui.viewHolders.tasks;

import android.view.View;

import com.habitrpg.android.habitica.models.tasks.Task;

import java.text.DateFormat;

public class TodoViewHolder extends ChecklistedViewHolder {

    private DateFormat dateFormatter;

    public TodoViewHolder(View itemView) {
        super(itemView);
        dateFormatter = android.text.format.DateFormat.getDateFormat(context);
    }

    @Override
    public void bindHolder(Task newTask, int position) {
        this.task = newTask;
        if (newTask.getCompleted()) {
            this.checklistIndicatorWrapper.setBackgroundColor(this.taskGray);
        } else {
            this.checklistIndicatorWrapper.setBackgroundResource(newTask.getLightTaskColor());
        }
        super.bindHolder(newTask, position);
    }

    @Override
    protected void configureSpecialTaskTextView(Task task) {
        if (this.specialTaskTextView != null) {
            if (task.getDueDate() != null) {
                this.specialTaskTextView.setText(dateFormatter.format(task.getDueDate()));
                this.specialTaskTextView.setVisibility(View.VISIBLE);
            } else {
                this.specialTaskTextView.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    public Boolean shouldDisplayAsActive(Task newTask) {
        return !newTask.getCompleted();
    }
}
