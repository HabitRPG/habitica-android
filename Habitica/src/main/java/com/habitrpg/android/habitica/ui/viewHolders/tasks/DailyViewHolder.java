package com.habitrpg.android.habitica.ui.viewHolders.tasks;

import android.view.View;
import android.widget.TextView;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.models.tasks.Task;

import butterknife.BindView;

public class DailyViewHolder extends ChecklistedViewHolder {

    @BindView(R.id.streakTextView)
    TextView streakTextView;

    public DailyViewHolder(View itemView) {
        super(itemView);
    }

    @Override
    public void bindHolder(Task newTask, int position) {
        if (newTask.isChecklistDisplayActive()) {
            this.checklistIndicatorWrapper.setBackgroundResource(newTask.getLightTaskColor());
        } else {
            this.checklistIndicatorWrapper.setBackgroundColor(this.taskGray);
        }
        super.bindHolder(newTask, position);
    }

    @Override
    public Boolean shouldDisplayAsActive(Task newTask) {
        return newTask.isDisplayedActive();
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
