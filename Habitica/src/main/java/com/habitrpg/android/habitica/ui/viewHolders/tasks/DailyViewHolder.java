package com.habitrpg.android.habitica.ui.viewHolders.tasks;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.models.tasks.Task;

import android.view.View;
import android.widget.TextView;

import butterknife.BindView;

public class DailyViewHolder extends ChecklistedViewHolder {

    private final int dailyResetOffset;
    @BindView(R.id.streakTextView)
    TextView streakTextView;

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

    @Override
    protected void configureSpecialTaskTextView(Task task) {
        super.configureSpecialTaskTextView(task);
        if (this.streakTextView != null) {
            if (task.streak != null && task.streak > 0) {
                this.streakTextView.setText(String.valueOf(task.streak));
                this.streakTextView.setVisibility(View.VISIBLE);
            } else {
                this.streakTextView.setVisibility(View.GONE);
            }
        }
    }

    @Override
    protected Boolean getTaskIconWrapperIsVisible() {
        Boolean isVisible = super.getTaskIconWrapperIsVisible();
        if (this.streakTextView.getVisibility() == View.VISIBLE) {
            isVisible = true;
        }
        return isVisible;
    }
}
