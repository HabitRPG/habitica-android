package com.habitrpg.android.habitica.ui.viewHolders.tasks;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.events.HabitScoreEvent;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Task;

import org.greenrobot.eventbus.EventBus;

import android.graphics.Color;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

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

    public HabitViewHolder(View itemView) {
        super(itemView);
    }

    @Override
    public void bindHolder(Task newTask, int position) {
        super.bindHolder(newTask, position);

        if (this.task.up) {
            this.btnPlusWrapper.setBackgroundResource(this.task.getLightTaskColor());
            this.btnPlusIconView.setImageResource(R.drawable.habit_plus);
        } else {
            this.btnPlusWrapper.setBackgroundResource(R.color.habit_inactive_gray);
            this.btnPlusIconView.setImageResource(R.drawable.habit_plus_disabled);
        }

        if (this.task.down) {
            this.btnMinusWrapper.setBackgroundResource(this.task.getLightTaskColor());
            this.btnMinusIconView.setImageResource(R.drawable.habit_minus);
        } else {
            this.btnMinusWrapper.setBackgroundResource(R.color.habit_inactive_gray);
            this.btnMinusIconView.setImageResource(R.drawable.habit_minus_disabled);
        }
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
}
