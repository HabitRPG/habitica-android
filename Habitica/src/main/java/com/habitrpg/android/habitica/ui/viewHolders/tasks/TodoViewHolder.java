package com.habitrpg.android.habitica.ui.viewHolders.tasks;

import com.habitrpg.android.habitica.R;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Task;

import android.view.View;

public class TodoViewHolder extends ChecklistedViewHolder {

    public TodoViewHolder(View itemView) {
        super(itemView);
    }

    @Override
    public void bindHolder(Task newTask, int position) {
        super.bindHolder(newTask, position);
        if (this.task.getCompleted()) {
            this.checklistIndicatorWrapper.setBackgroundResource(this.task.getLightTaskColor());
        } else {
            this.checklistIndicatorWrapper.setBackgroundColor(this.taskGray);
        }
    }

    @Override
    public Boolean shouldDisplayAsActive() {
        return this.task.getCompleted();
    }
}
