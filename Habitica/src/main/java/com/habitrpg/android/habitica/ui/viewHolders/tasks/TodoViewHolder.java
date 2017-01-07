package com.habitrpg.android.habitica.ui.viewHolders.tasks;

import com.magicmicky.habitrpgwrapper.lib.models.tasks.Task;

import android.view.View;

import java.text.DateFormat;

public class TodoViewHolder extends ChecklistedViewHolder {

    private DateFormat dateFormatter;

    public TodoViewHolder(View itemView) {
        super(itemView);
        dateFormatter = android.text.format.DateFormat.getDateFormat(context);
    }

    @Override
    public void bindHolder(Task newTask, int position) {
        super.bindHolder(newTask, position);
        if (this.task.getCompleted()) {
            this.checklistIndicatorWrapper.setBackgroundColor(this.taskGray);
        } else {
            this.checklistIndicatorWrapper.setBackgroundResource(this.task.getLightTaskColor());
        }

    }

    @Override
    protected void configureSpecialTaskTextView(Task task) {
        if (this.specialTaskTextView != null) {
            if (task.duedate != null) {
                this.specialTaskTextView.setText(dateFormatter.format(task.duedate));
                this.specialTaskTextView.setVisibility(View.VISIBLE);
            } else {
                this.specialTaskTextView.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    public Boolean shouldDisplayAsActive() {
        return !this.task.getCompleted();
    }
}
