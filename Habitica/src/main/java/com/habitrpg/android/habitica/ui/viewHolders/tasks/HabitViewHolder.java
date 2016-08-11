package com.habitrpg.android.habitica.ui.viewHolders.tasks;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.events.HabitScoreEvent;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Task;

import org.greenrobot.eventbus.EventBus;

import android.graphics.Color;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import butterknife.BindView;
import butterknife.OnClick;

public class HabitViewHolder extends BaseTaskViewHolder {

    @BindView(R.id.btnPlusWrapper)
    FrameLayout btnPlusWrapper;
    @BindView(R.id.btnPlusIconView)
    View btnPlusIconView;
    @BindView(R.id.btnPlusBackground)
    View btnPlusBackground;
    @BindView(R.id.btnPlus)
    Button btnPlus;

    @BindView(R.id.btnMinusWrapper)
    FrameLayout btnMinusWrapper;
    @BindView(R.id.btnMinusIconView)
    View btnMinusIconView;
    @BindView(R.id.btnMinusBackground)
    View btnMinusBackground;
    @BindView(R.id.btnMinus)
    Button btnMinus;

    public HabitViewHolder(View itemView) {
        super(itemView);
    }

    @Override
    public void bindHolder(Task newTask, int position) {
        super.bindHolder(newTask, position);

        this.btnPlusWrapper.setVisibility(this.task.getUp() ? View.VISIBLE : View.GONE);
        this.btnPlusBackground.setBackgroundResource(this.task.getLightTaskColor());

        this.btnMinusWrapper.setVisibility(this.task.getDown() ? View.VISIBLE : View.GONE);
        if (task.getUp()) {
            this.btnMinusBackground.setBackgroundResource(this.task.getMediumTaskColor());
        } else {
            this.btnMinusBackground.setBackgroundResource(this.task.getLightTaskColor());
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
