package com.habitrpg.android.habitica.ui.viewHolders.tasks;

import com.habitrpg.android.habitica.R;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Task;

import android.view.View;

public class DailyViewHolder extends ChecklistedViewHolder {

    public final int dailyResetOffset;

    public DailyViewHolder(View itemView, int dailyResetOffset) {
        super(itemView);
        this.dailyResetOffset = dailyResetOffset;
    }

    @Override
    public void bindHolder(Task newTask, int position) {
        super.bindHolder(newTask, position);
        if (this.task.isChecklistDisplayActive(dailyResetOffset)) {
            this.checklistIndicatorWrapper.setBackgroundResource(this.task.getLightTaskColor());
        } else {
            this.checklistIndicatorWrapper.setBackgroundColor(this.taskGray);
        }
    }

    @Override
    public Boolean shouldDisplayAsActive() {
        return this.task.isDisplayedActive(this.dailyResetOffset);
    }
}
