package com.habitrpg.android.habitica.ui.viewHolders.tasks;

import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;

import com.facebook.drawee.view.SimpleDraweeView;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.events.TaskTappedEvent;
import com.habitrpg.android.habitica.events.commands.BuyRewardCommand;
import com.habitrpg.android.habitica.models.tasks.Task;
import com.habitrpg.android.habitica.ui.ItemDetailDialog;
import com.habitrpg.android.habitica.ui.helpers.DataBindingUtils;

import org.greenrobot.eventbus.EventBus;

import java.text.DecimalFormat;

import butterknife.BindView;
import butterknife.OnClick;

public class RewardViewHolder extends BaseTaskViewHolder {

    private final DecimalFormat priceFormat;
    @BindView(R.id.rewardImageView)
    SimpleDraweeView rewardImageView;

    @BindView(R.id.btnReward)
    Button rewardButton;

    private Drawable customRewardIcon;

    public RewardViewHolder(View itemView) {
        super(itemView);
        priceFormat = new DecimalFormat("0.##");

        customRewardIcon = ContextCompat.getDrawable(itemView.getContext(), R.drawable.custom_reward);
    }

    @Override
    public void bindHolder(Task newTask, int position) {
        super.bindHolder(newTask, position);

        this.rewardButton.setText(this.priceFormat.format(this.task.value));

        if (this.isItem()) {
            DataBindingUtils.loadImage(this.rewardImageView, "shop_" + this.task.getId());
        } else {
            this.rewardImageView.setImageDrawable(customRewardIcon);
        }
    }

    private boolean isItem() {
        return this.task.specialTag != null && this.task.specialTag.equals("item");
    }

    @Override
    public boolean canContainMarkdown() {
        return !isItem();
    }

    @OnClick(R.id.btnReward)
    void buyReward() {
        BuyRewardCommand event = new BuyRewardCommand();
        event.Reward = task;
        EventBus.getDefault().post(event);
    }

    @Override
    public void onClick(View v) {
        if (task.specialTag != null && task.specialTag.equals("item")) {
            ItemDetailDialog dialog = new ItemDetailDialog(context);
            dialog.setTitle(task.getText());
            dialog.setDescription(task.getNotes());
            dialog.setImage("shop_" + this.task.getId());
            dialog.setCurrency("gold");
            dialog.setValue(task.getValue());
            dialog.setBuyListener((clickedDialog, which) -> {
                this.buyReward();
            });
            dialog.show();
        } else {
            TaskTappedEvent event = new TaskTappedEvent();
            event.Task = task;

            EventBus.getDefault().post(event);
        }
    }

    @Override
    public void setDisabled(boolean openTaskDisabled, boolean taskActionsDisabled) {
        super.setDisabled(openTaskDisabled, taskActionsDisabled);

        this.rewardButton.setEnabled(!taskActionsDisabled);
    }

}
