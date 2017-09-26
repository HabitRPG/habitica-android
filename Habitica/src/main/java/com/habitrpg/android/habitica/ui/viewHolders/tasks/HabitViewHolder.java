package com.habitrpg.android.habitica.ui.viewHolders.tasks;

import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.events.HabitScoreEvent;
import com.habitrpg.android.habitica.models.tasks.Task;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.OnClick;

public class HabitViewHolder extends BaseTaskViewHolder {

    @BindView(R.id.btnPlusWrapper)
    FrameLayout btnPlusWrapper;
    @BindView(R.id.btnPlusIconView)
    ImageView btnPlusIconView;
    @BindView(R.id.btnPlus)
    Button btnPlus;

    @BindView(R.id.btnMinusWrapper)
    FrameLayout btnMinusWrapper;
    @BindView(R.id.btnMinusIconView)
    ImageView btnMinusIconView;
    @BindView(R.id.btnMinus)
    Button btnMinus;

    @BindView(R.id.streakTextView)
    TextView streakTextView;

    public HabitViewHolder(View itemView) {
        super(itemView);
    }

    @Override
    public void bindHolder(Task newTask, int position) {
        this.task = newTask;
        if (newTask.up) {
            this.btnPlusWrapper.setBackgroundResource(newTask.getLightTaskColor());
            if (newTask.getLightTaskColor() == R.color.yellow_100) {
                this.btnPlusIconView.setImageResource(R.drawable.habit_plus_yellow);
            } else {
                this.btnPlusIconView.setImageResource(R.drawable.habit_plus);
            }
            this.btnPlus.setVisibility(View.VISIBLE);
            this.btnPlus.setClickable(true);
        } else {
            this.btnPlusWrapper.setBackgroundResource(R.color.habit_inactive_gray);
            this.btnPlusIconView.setImageResource(R.drawable.habit_plus_disabled);
            this.btnPlus.setVisibility(View.GONE);
            this.btnPlus.setClickable(false);
        }

        if (newTask.down) {
            this.btnMinusWrapper.setBackgroundResource(newTask.getLightTaskColor());
            if (newTask.getLightTaskColor() == R.color.yellow_100) {
                this.btnMinusIconView.setImageResource(R.drawable.habit_minus_yellow);
            } else {
                this.btnMinusIconView.setImageResource(R.drawable.habit_minus);
            }
            this.btnMinus.setVisibility(View.VISIBLE);
            this.btnMinus.setClickable(true);
        } else {
            this.btnMinusWrapper.setBackgroundResource(R.color.habit_inactive_gray);
            this.btnMinusIconView.setImageResource(R.drawable.habit_minus_disabled);
            this.btnMinus.setVisibility(View.GONE);
            this.btnMinus.setClickable(false);
        }

        String streakString = "";
        if (newTask.counterUp > 0 && newTask.counterDown > 0) {
            streakString = streakString + "+" + String.valueOf(task.counterUp) + " | -" + String.valueOf(task.counterDown);
        } else if (newTask.counterUp > 0) {
            streakString = streakString + "+" + String.valueOf(task.counterUp);
        } else if (newTask.counterUp > 0) {
            streakString = streakString + "-" + String.valueOf(task.counterDown);
        }
        if (streakString.length() > 0) {
            streakTextView.setText(streakString);
            streakTextView.setVisibility(View.VISIBLE);
        } else {
            streakTextView.setVisibility(View.GONE);
        }
        super.bindHolder(newTask, position);
    }

    @OnClick(R.id.btnPlus)
    public void onPlusButtonClicked() {
        HabitScoreEvent event = new HabitScoreEvent();
        event.Up = true;
        event.habit = task;
        EventBus.getDefault().post(event);
    }

    @OnClick(R.id.btnMinus)
    public void onMinusButtonClicked() {
        HabitScoreEvent event = new HabitScoreEvent();
        event.Up = false;
        event.habit = task;
        EventBus.getDefault().post(event);
    }

    @Override
    public void setDisabled(boolean openTaskDisabled, boolean taskActionsDisabled) {
        super.setDisabled(openTaskDisabled, taskActionsDisabled);

        this.btnPlus.setEnabled(!taskActionsDisabled);
        this.btnMinus.setEnabled(!taskActionsDisabled);
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
