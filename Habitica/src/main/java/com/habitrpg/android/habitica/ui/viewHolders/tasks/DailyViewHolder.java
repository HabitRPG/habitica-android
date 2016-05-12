package com.habitrpg.android.habitica.ui.viewHolders.tasks;

import com.habitrpg.android.habitica.R;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Task;

import android.view.View;
import android.widget.TextView;

import butterknife.BindView;

public class DailyViewHolder extends ChecklistedViewHolder {

    @BindView(R.id.streakTextView)
    TextView streakTextView;

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
        if (task.streak != null && task.streak > 0) {
            this.streakTextView.setText(itemView.getContext().getString(R.string.daily_streak, task.streak));
            this.streakTextView.setVisibility(View.VISIBLE);
        } else {
            this.streakTextView.setVisibility(View.GONE);
        }
    }

    @Override
    public Boolean shouldDisplayAsActive() {
        return this.task.isDisplayedActive(this.dailyResetOffset);
    }
}
